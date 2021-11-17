package com.spolks.server.di.modules.socket.tcp

import com.spolks.server.config.ServerConfig
import com.spolks.server.socket.impl.tcp.TCPServerSocket
import org.kodein.di.*

val tcpSocketModule = DI.Module("TcpSocket") {
    bind<TCPServerSocket>() with singleton {
        val config = instance<ServerConfig>()

        TCPServerSocket(config.socketScope, config.hostname, config.tcpPort)
    }
}
