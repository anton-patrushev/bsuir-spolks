package com.astronaut.server.socket.impl.udp

import com.astronaut.common.socket.udp.QueuedDatagramPacket
import com.astronaut.common.utils.toByteArray
import io.ktor.network.sockets.*
import kotlinx.coroutines.delay
import java.net.InetSocketAddress
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class UDPClientsPool {
    private val users: MutableMap<String, UDPClientSocket> = mutableMapOf()
    private val newUsers: MutableMap<String, UDPClientSocket> = mutableMapOf()

    private lateinit var sendDelegate: suspend (ByteArray, InetSocketAddress) -> Unit

    fun addOrUpdateClient(data: QueuedDatagramPacket) {
        if(users.contains(data.address.toString())) {
            users.getValue(data.address.toString()).setReadData(data.data.toByteArray())
        } else {
            val connection = UDPClientSocket(
                data.address,
                data.data.toByteArray(),
                sendDelegate
            ) {
                users.remove(data.address.toString())
            }

            newUsers[data.address.toString()] = connection
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getNewClient(): UDPClientSocket {
        while (newUsers.isEmpty()) {
            delay(Duration.Companion.nanoseconds(1))
        }

        val user = newUsers.keys.elementAt(0);
        val udp = newUsers.getValue(user)

        users[user] = udp
        newUsers.remove(user)

        return udp
    }

    fun setSendDelegate(delegate: suspend (ByteArray, InetSocketAddress) -> Unit) {
        sendDelegate = delegate
    }
}
