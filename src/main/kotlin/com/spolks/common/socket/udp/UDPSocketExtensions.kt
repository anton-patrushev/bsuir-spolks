package com.astronaut.common.socket.udp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import kotlin.coroutines.coroutineContext


/**
 * Executes [UDPSocket.runOnce] in loop until coroutine is not canceled
 */
suspend fun UDPSocket.runSuspending() {
    while (coroutineContext.isActive) {
        runOnce()
        delay(1)
    }
}

/**
 * [UDPSocket.send] but instead of [ByteBuffer] it sends [ByteArray]
 *
 * @param data [ByteArray] - input data
 * @param to [InetSocketAddress] - receiver
 *
 * @return [UDPSendContext]
 */
suspend fun UDPSocket.send(data: ByteArray, to: InetSocketAddress): UDPSendContext {
    val buffer = ByteBuffer.allocate(data.size)

    buffer.put(data)
    buffer.flip()

    return send(buffer, to)
}

suspend fun UDPSocket.listenForNewPackages(): Flow<QueuedDatagramPacket> {
    val socket = this

    return flow {
        while (coroutineContext.isActive) {
            emit(socket.receive())
            delay(1)
        }
    }
}