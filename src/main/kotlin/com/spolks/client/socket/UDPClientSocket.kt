package com.spolks.client.socket

import com.spolks.common.socket.udp.UDPSocket
import com.spolks.common.socket.udp.runSuspending
import com.spolks.common.socket.udp.send
import com.spolks.common.utils.Events
import com.spolks.common.utils.getUnifiedString
import com.spolks.common.utils.toByteArray
import java.net.InetSocketAddress

class UDPClientSocket(
    private val socket: UDPSocket,
    private val address: InetSocketAddress
) {
    suspend fun sendEvent(e: Events) {
        socket.send(e.toString().encodeToByteArray(), address)
    }

    suspend fun receiveEvent(): Events {
        return Events.parseFromClientString(receiveString())
    }

    private suspend fun receiveString(): String {
        return socket.receive().data.toByteArray().getUnifiedString()
    }

    suspend fun receiveByteArray(buffer: ByteArray): Int {
        return try {
            socket.receive().data.toByteArray().copyInto(buffer)
            buffer.size
        } catch (e: Throwable) {
            e.printStackTrace()
            -1
        }
    }

    suspend fun sendByteArray(data: ByteArray) {
        socket.send(data, address)
    }

    suspend fun runSuspending() {
        socket.runSuspending()
    }
}
