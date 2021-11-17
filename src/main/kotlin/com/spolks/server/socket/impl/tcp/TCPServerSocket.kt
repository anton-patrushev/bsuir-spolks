package com.astronaut.server.socket.impl.tcp

import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.socket.ServerSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import java.net.InetSocketAddress

class TCPServerSocket(
    appScope: CoroutineScope,
    hostname: String,
    port: Int
): ServerSocket {

    private val socket: io.ktor.network.sockets.ServerSocket =
        aSocket(ActorSelectorManager(appScope.coroutineContext))
            .tcp()
            .bind(InetSocketAddress(hostname, port))

    override suspend fun accept(): ClientSocket {
        val raw = socket.accept()

        return TCPClientSocket(raw)
    }

}