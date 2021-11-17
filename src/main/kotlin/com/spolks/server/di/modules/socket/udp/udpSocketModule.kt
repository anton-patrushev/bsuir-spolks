package com.spolks.server.di.modules.socket.udp

import com.spolks.server.config.ServerConfig
import com.spolks.server.socket.impl.udp.UDPClientsPool
import com.spolks.server.socket.impl.udp.UDPServerSocket
import org.kodein.di.*

val udpSocketModule = DI.Module("UdpSocket") {
    bind<UDPClientsPool>() with singleton { UDPClientsPool() }

    bind<UDPServerSocket>() with singleton {
        val config = instance<ServerConfig>()

        UDPServerSocket(
            config.socketScope,
            instance(),
            config.hostname,
            config.udpPort
        )
    }
}
