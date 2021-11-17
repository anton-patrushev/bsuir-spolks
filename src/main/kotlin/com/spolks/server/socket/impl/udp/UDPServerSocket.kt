package com.spolks.server.socket.impl.udp

import com.spolks.common.repository.impl.CHUNK_SIZE
import com.spolks.common.socket.udp.UDPSocket
import com.spolks.common.socket.udp.listenForNewPackages
import com.spolks.common.socket.udp.runSuspending
import com.spolks.common.socket.udp.send
import com.spolks.server.socket.ClientSocket
import com.spolks.server.socket.ServerSocket
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
