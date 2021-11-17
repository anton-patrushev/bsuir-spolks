package com.spolks.server.di.modules.server

import com.spolks.server.config.ServerConfig
import com.spolks.server.server.Server
import com.spolks.server.server.impl.ServerImpl
import com.spolks.server.server.ServerSocketWrapper
import com.spolks.server.server.impl.ServerSocketWrapperImpl
import com.spolks.server.socket.ServerSocket
import com.spolks.server.socket.impl.tcp.TCPServerSocket
import com.spolks.server.socket.impl.udp.UDPServerSocket
import com.spolks.server.utils.ServerProtocol
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
