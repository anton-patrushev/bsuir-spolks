package com.astronaut.server.di.modules.socket.tcp

import com.astronaut.server.config.ServerConfig
import com.astronaut.server.socket.impl.tcp.TCPServerSocket
import org.kodein.di.*

val tcpSocketModule = DI.Module("TcpSocket") {
    bind<TCPServerSocket>() with singleton {
        val config = instance<ServerConfig>()

        TCPServerSocket(config.socketScope, config.hostname, config.tcpPort)
    }
}
