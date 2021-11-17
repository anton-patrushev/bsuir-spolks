package com.astronaut.server.socket.impl.udp

import com.astronaut.common.repository.impl.CHUNK_SIZE
import com.astronaut.common.socket.udp.UDPSocket
import com.astronaut.common.socket.udp.listenForNewPackages
import com.astronaut.common.socket.udp.runSuspending
import com.astronaut.common.socket.udp.send
import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.socket.ServerSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.net.InetSocketAddress

class UDPServerSocket(
    appScope: CoroutineScope,
    private val pool: UDPClientsPool,
    hostname: String,
    port: Int
): ServerSocket {
    private val socket: UDPSocket = UDPSocket(mtuBytes = CHUNK_SIZE,
        windowSizeBytes = CHUNK_SIZE * 100,
        congestionControlTimeoutMs = 1,
    )

    init {
        socket.bind(InetSocketAddress(hostname, port))

        appScope.launch {
            pool.setSendDelegate { data, address ->
                socket.send(data, address)
            }

            launch { socket.runSuspending() }

            launch {
                socket.listenForNewPackages()
                    .collect {
                        pool.addOrUpdateClient(it)
                    }
            }
        }
    }

    override suspend fun accept(): ClientSocket {
        return pool.getNewClient()
    }
}
