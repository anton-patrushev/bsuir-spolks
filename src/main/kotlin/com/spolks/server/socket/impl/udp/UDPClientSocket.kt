package com.astronaut.server.socket.impl.udp

import com.astronaut.common.utils.WindowingHandler
import com.astronaut.common.utils.getUnifiedString
import com.astronaut.server.socket.ClientSocket
import io.ktor.network.sockets.*
import io.ktor.util.collections.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import kotlin.coroutines.coroutineContext

class UDPClientSocket(
    private val address: InetSocketAddress,
    readData: ByteArray?,
    private val onWrite: suspend (ByteArray, InetSocketAddress) -> Unit,
    private val onClose: () -> Unit,
): ClientSocket {

    private var isActive: Boolean = true
    private val mutex = Mutex()
    private val inputBuffer = ConcurrentList<ByteArray>()

    init {
        readData?.let {
            inputBuffer.add(it)
        }
    }

    private suspend fun waitBeforeRead() {
        try {
            // If data to read is null - lock until it will be unlocked in setReadData method
            if(inputBuffer.size == 0) {
                if(!isActive) {
                    return
                }

                // Dont wait for unlock here if its locked (it means that readString was called earlier than setReadData)
                if(!mutex.isLocked) {
                    mutex.lock()
                }
            } else {
                // If data exists - unlock mutex
                if(mutex.isLocked) {
                    mutex.unlock()
                }
            }

            // Lock mutex and read from datagram if data exists, wait for unlock if data is null
            mutex.lock()
        } catch (e: Throwable) {
            if(mutex.isLocked) {
                mutex.unlock()
            }
        }
    }

    private suspend fun internalReadByteArray(): ByteArray {
        waitBeforeRead()

        if(!isActive && inputBuffer.size == 0) {
            return byteArrayOf()
        }

        return inputBuffer.removeAt(0)
    }

    private suspend fun internalWriteByteArray(data: ByteArray) {
        onWrite(data, address)
    }

    override suspend fun readString(): String {
        val data = internalReadByteArray()

        return data.getUnifiedString()
    }

    override suspend fun readByteArray(data: ByteArray): Int? {
        internalReadByteArray().copyInto(data)

        return data.size
    }

    override suspend fun writeString(data: String) {
        internalWriteByteArray(data.encodeToByteArray())
    }

    override suspend fun writeByteArray(data: ByteArray) {
        internalWriteByteArray(data)
    }

    override suspend fun close() {
        isActive = false
        mutex.unlock()
        onClose()
    }

    fun setReadData(data: ByteArray) {
        inputBuffer.add(data)

        try {
            if(mutex.isLocked) mutex.unlock()
        } catch (e: Throwable) {

        }
    }
}