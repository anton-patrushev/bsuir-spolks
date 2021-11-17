package com.astronaut.server.config

import com.astronaut.server.utils.ServerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class ServerConfig {
    lateinit var appScope: CoroutineScope
        private set

    lateinit var socketScope: CoroutineScope

    var isMultithreaded: Boolean = false
        private set

    var serverProtocol: ServerProtocol? = ServerProtocol.TCP
        private set

    var hostname: String = "0.0.0.0"
        private set

    var tcpPort: Int = 2323
        private set

    var udpPort: Int = 2324
        private set

    var isSynchronous: Boolean = false
        private set

    fun configure(
        protocol: ServerProtocol? = null,
        isMultithreaded: Boolean = false,
        isSynchronous: Boolean = false,
        coroutineScope: CoroutineScope? = null
    ) {
        serverProtocol = protocol
        this.isSynchronous = isSynchronous
        this.isMultithreaded = isMultithreaded

        appScope = coroutineScope
            ?: CoroutineScope(
                (if(isMultithreaded)
                    Executors.newCachedThreadPool()
                else
                    Executors.newSingleThreadExecutor())
                    .asCoroutineDispatcher()
            )

        socketScope = if(!isMultithreaded && !isSynchronous) {
            CoroutineScope(Executors.newFixedThreadPool(2).asCoroutineDispatcher())
        } else {
            appScope
        }
    }
}
