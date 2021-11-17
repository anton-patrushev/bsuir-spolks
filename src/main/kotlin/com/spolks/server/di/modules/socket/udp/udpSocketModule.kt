package com.astronaut.server.di.modules.socket.udp

import com.astronaut.server.config.ServerConfig
import com.astronaut.server.socket.impl.udp.UDPClientsPool
import com.astronaut.server.socket.impl.udp.UDPServerSocket
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
