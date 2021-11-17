package com.astronaut.server.di.modules.server

import com.astronaut.server.config.ServerConfig
import com.astronaut.server.server.Server
import com.astronaut.server.server.impl.ServerImpl
import com.astronaut.server.server.ServerSocketWrapper
import com.astronaut.server.server.impl.ServerSocketWrapperImpl
import com.astronaut.server.socket.ServerSocket
import com.astronaut.server.socket.impl.tcp.TCPServerSocket
import com.astronaut.server.socket.impl.udp.UDPServerSocket
import com.astronaut.server.utils.ServerProtocol
import org.kodein.di.*

val serverModule = DI.Module("Server") {
    bind<ServerSocketWrapper>() with factory { socket: ServerSocket ->
        ServerSocketWrapperImpl(socket)
    }

    bind<Server>() with singleton {
        val config: ServerConfig = instance()

        config.serverProtocol

        var includeTCP = false
        var includeUDP = false

        if(config.serverProtocol == ServerProtocol.TCP || config.serverProtocol == null)
            includeTCP = true

        if(config.serverProtocol == ServerProtocol.UDP || config.serverProtocol == null)
            includeUDP = true

        ServerImpl(
            if(includeTCP) instance(arg = instance<TCPServerSocket>()) else null,
            if(includeUDP) instance(arg = instance<UDPServerSocket>()) else null,
            instance(),
            instance(),
            instance()
        )
    }
}
