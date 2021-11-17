package com.spolks.server.socket.impl.udp

import com.spolks.common.utils.getUnifiedString
import com.spolks.server.socket.ClientSocket
import io.ktor.util.collections.*
import kotlinx.coroutines.sync.Mutex
import java.net.InetSocketAddress

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