package com.spolks.common.socket.udp

import com.spolks.common.utils.wirehair.Wirehair
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * The socket itself. Just create one of those and use it to send and receive data over the network.
 *
 * @param mtuBytes [Int] - minimum MTU of all those router between you and someone you send data to
 * @param windowSizeBytes [Int] - pieces of data are sent in small groups with total size of this value
 * @param congestionControlTimeoutMs [Long] - after each group of [windowSizeBytes] is sent, socket waits until
 *  [congestionControlTimeoutMs] elapsed before sending another [windowSizeBytes] of that data
 * @param cleanUpTimeoutMs [Long] - the lesser this value is, the more frequent socket will clean up itself
 */
class UDPSocket(
    val mtuBytes: Int = 1200,
    val windowSizeBytes: Int = 4800,
    val congestionControlTimeoutMs: Long = 100,
    val cleanUpTimeoutMs: Long = (1000 * 60 * 10).toLong()
) {
    companion object {
        var count = 0

        init {
            println()
            Wirehair.init()
        }
    }

    private val logger = KotlinLogging.logger("UDPSocket-${++count}")

    /* --- High-level UDP stuff --- */

    val sendQueue = ConcurrentLinkedQueue<PacketAndContinuation>()
    val receiveQueue = Channel<QueuedDatagramPacket>()
    val contextManager = UDPContextManager()
    val repairBlockSizeBytes = mtuBytes - RepairBlock.METADATA_SIZE_BYTES

    /**
     * Adds data in processing queue for send. Suspends until data is certainly sent. Can be canceled.
     *
     * @param data [ByteBuffer] - normalized (flipped) data
     * @param to [InetSocketAddress] - address to send data to
     *
     * @return [UDPSendContext]
     */
    suspend fun send(data: ByteBuffer, to: InetSocketAddress) = suspendCancellableCoroutine<

            UDPSendContext> { cont ->
        sendQueue.add(QueuedDatagramPacket(data, to) to cont)
    }

    /**
     * Suspends until there is a packet to receive
     *
     * @return [QueuedDatagramPacket]
     */
    suspend fun receive() = receiveQueue.receive()

    /**
     * Runs processing loop once. Suspends if nobody receives packets.
     *
     * Loop consists of three stages:
     *  1. Clean up
     *  2. Processing send
     *  3. Processing receive
     */
    @ExperimentalCoroutinesApi
    suspend fun runOnce() {
        // clean up
        contextManager.cleanUpFinishedReceiveContexts(cleanUpTimeoutMs)
        contextManager.destroyAllForbiddenReceiveContexts(cleanUpTimeoutMs)
        contextManager.destroyAllCanceledSendContexts()

        // send
        prepareSend()
        processSend()

        // receive
        val blocks = prepareReceive()
        processReceive(blocks)
    }

    private fun processSend() {
        val contexts = contextManager.getAllSendContexts()

        contexts.asSequence()
            .filter { it.isCongestionControlTimeoutElapsed(congestionControlTimeoutMs) } // TODO: get from CC
            .map {
                it.getNextWindowSizeRepairBlocks(
                    windowSizeBytes,
                    mtuBytes
                ) to it.packet.address
            } // TODO: get from CC
            .forEach { (blocks, to) ->
                blocks.forEach { block ->
                    //logger.trace { "Sending REPAIR_BLOCK threadId: ${block.threadId} blockId: ${block.blockId}" }
                    block.serialize(sendBuffer)
                    write(sendBuffer, to)
                }
            }
    }

    private fun prepareSend() {
        val sendQueueCopy = mutableListOf<PacketAndContinuation>()

        while (true) {
            val packet = sendQueue.poll() ?: break

            sendQueueCopy.add(packet)
        }

        sendQueueCopy.forEach { (packet, future) ->
            val threadId = UUID.randomUUID()

            //logger.trace { "Transmission for threadId: $threadId is started" }

            contextManager.createOrGetSendContext(threadId, packet, repairBlockSizeBytes, future)
        }
    }

    @ExperimentalCoroutinesApi
    private fun prepareReceive(): List<Pair<RepairBlock, InetSocketAddress>> {
        val packets = mutableListOf<QueuedDatagramPacket>()
        while (true) {
            val packet = read() ?: break

            packets.add(packet)
        }

        return packets.mapNotNull { packet ->
            when (parseFlag(packet.data)) {
                Flags.ACK -> {
                    val ack = parseAck(packet.data)

                    if (!contextManager.sendContextExists(ack.threadId))
                        return@mapNotNull null

                    val context = contextManager.getSendContext(ack.threadId)!!

                    context.ackReceived = true

                    //logger.trace { "Received ACK for threadId: ${ack.threadId}, stopping..." }

                    context.continuation.resume(context) {
                        //.warn { "Send for ${ack.threadId} was already canceled" }
                    }

                    contextManager.destroySendContext(context.threadId)

                    null
                }
                Flags.BLOCK_ACK -> {
                    val blockAck = parseBlockAck(packet.data)

                   // logger.trace { "Received BLOCK_ACK message for threadId: ${blockAck.threadId} from: ${packet.address}" }

                    /* TODO: handle congestion control tune */

                    null
                }
                Flags.REPAIR -> {
                    val block = parseRepairBlock(packet.data)

                   // logger.trace { "Received REPAIR_BLOCK message for threadId: ${block.threadId} blockId: ${block.blockId} from: ${packet.address}" }

                    block to packet.address
                }
                else -> null
            }
        }
    }

    private suspend fun processReceive(blocks: List<Pair<RepairBlock, InetSocketAddress>>) {
        blocks.forEach { (block, from) ->
            val threadId = block.threadId

            val context = if (contextManager.isReceiveContextFinished(block.threadId)) {
               // logger.trace { "Received a repair block for already received threadId: ${block.threadId}, skipping..." }
                sendAck(threadId, from)
                return@forEach
            } else {
                contextManager.createOrGetReceiveContext(threadId, block.messageSizeBytes, block.blockSizeBytes)
            }

            contextManager.updateReceiveContext(threadId)

            sendBlockAck(threadId, block.blockId, from)

            val message = context.tryToRecoverFrom(block) ?: return@forEach

            contextManager.markReceiveContextAsFinished(threadId)
            contextManager.destroyReceiveContext(threadId)

            sendAck(threadId, from)

            receiveQueue.send(QueuedDatagramPacket(message, from))
        }
    }

    private fun sendAck(threadId: UUID, to: InetSocketAddress) {
       // logger.trace { "Sending ACK for threadId: $threadId to $to" }

        val ack = Ack(threadId, 0F) // TODO: get from congestion index

        ack.serialize(sendBuffer)

        write(sendBuffer, to)
    }

    private fun sendBlockAck(threadId: UUID, blockId: Int, to: InetSocketAddress) {
       // logger.trace { "Sending BLOCK_ACK for threadId: $threadId to $to" }

        val blockAck = BlockAck(threadId, blockId, 0F) // TODO: get from congestion index

        blockAck.serialize(sendBuffer)

        write(sendBuffer, to)
    }

    private fun parseRepairBlock(buffer: ByteBuffer) = RepairBlock.deserialize(buffer)
    private fun parseAck(buffer: ByteBuffer) = Ack.deserialize(buffer)
    private fun parseBlockAck(buffer: ByteBuffer) = BlockAck.deserialize(buffer)
    private fun parseFlag(buffer: ByteBuffer) = buffer.get()

    /* --- Low-level plain UDP stuff --- */

    private var channel = DatagramChannel.open()
    private val sendBuffer = ByteBuffer.allocateDirect(mtuBytes)
    private val receiveBuffer = ByteBuffer.allocateDirect(mtuBytes)
    private var state = SocketState.UNBOUND

    init {
        channel.configureBlocking(false)
    }

    /**
     * Binds to the local address. Before this call you're unable to receive packets.
     *
     * @param on [InetSocketAddress] - address to bind
     */
    fun bind(on: InetSocketAddress) = synchronized(state) {
        throwIfClosed()
        channel.bind(on)
        state = SocketState.BOUND
    }

    /**
     * Destroys all contexts and closes this socket - after this you should create another one to work with
     */
    fun close() = synchronized(state) {
        contextManager.destroyAllContexts()

        channel.close()
        state = SocketState.CLOSED
    }

    /**
     * Get socket state
     *
     * @return [SocketState]
     */
    fun getState() = state

    /**
     * Is socket closed
     *
     * @return [Boolean]
     */
    fun isClosed() = state == SocketState.CLOSED

    private fun write(data: ByteBuffer, address: InetSocketAddress) = synchronized(state) {
       // logger.trace { "Sending ${data.limit()} bytes to $address" }
        throwIfClosed()
        channel.send(data, address)

        data.clear()
    }

    private fun read(): QueuedDatagramPacket? = synchronized(state) {
        throwIfNotBound()
        val remoteAddress = channel.receive(receiveBuffer)

        if (receiveBuffer.position() == 0) return null

        val size = receiveBuffer.position()

        receiveBuffer.flip()

       // logger.trace { "Receiving $size bytes from $remoteAddress" }

        val data = ByteBuffer.allocate(size)

        data.put(receiveBuffer)
        data.flip()

        receiveBuffer.clear()

        val from = InetSocketAddress::class.java.cast(remoteAddress)

        QueuedDatagramPacket(data, from)
    }

    private fun throwIfNotBound() = check(state == SocketState.BOUND) { "Socket should be BOUND" }
    private fun throwIfClosed() = check(state != SocketState.CLOSED) { "Socket is closed" }
}
