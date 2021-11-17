package com.astronaut.server.di.modules.socket

import com.astronaut.server.config.ServerConfig
import com.astronaut.server.di.modules.socket.tcp.tcpSocketModule
import com.astronaut.server.di.modules.socket.udp.udpSocketModule
import com.astronaut.server.socket.ServerSocket
import com.astronaut.server.socket.impl.tcp.TCPServerSocket
import com.astronaut.server.socket.impl.udp.UDPServerSocket
import com.astronaut.server.utils.ServerProtocol
import org.kodein.di.*

val socketModule = DI.Module("Socket") {
    import(tcpSocketModule)
    import(udpSocketModule)

    /*bind<ServerSocket>() with singleton {
        val config = instance<ServerConfig>()

        when(config.serverProtocol) {
            ServerProtocol.TCP -> instance<TCPServerSocket>()
            ServerProtocol.UDP -> instance<UDPServerSocket>()
        }
    }*/
}
