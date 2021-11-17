package com.spolks.server.di.modules.socket

import com.spolks.server.di.modules.socket.tcp.tcpSocketModule
import com.spolks.server.di.modules.socket.udp.udpSocketModule
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
