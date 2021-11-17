package com.spolks.server.server.impl

import com.spolks.server.config.ServerConfig
import com.spolks.server.controller.BaseController
import com.spolks.server.controller.FileController
import com.spolks.server.server.Server
import com.spolks.server.server.ServerSocketWrapper
import com.spolks.server.socket.ClientSocket
import com.spolks.common.utils.Events
import kotlinx.coroutines.*

class ServerImpl(
    private val tcpSocket: ServerSocketWrapper?,
    private val udpSocket: ServerSocketWrapper?,
    private val config: ServerConfig,
    private val baseController: BaseController,
    private val fileController: FileController
): Server {
    override fun start() {
        if(config.isSynchronous && !config.isMultithreaded) { // Single-thread sync
            runBlocking {
                bootstrap(tcpSocket ?: udpSocket!! , this)
            }
        } else if(!config.isSynchronous && !config.isMultithreaded) { // Single-thread async
            tcpSocket?.let {
                config.appScope.launch {
                    bootstrap(it,this)
                }
            }

            udpSocket?.let {
                config.appScope.launch {
                    bootstrap(it,this)
                }
            }
        } else { // Multi-thread
            config.appScope.launch {
                bootstrap(tcpSocket!!,this)
            }
        }
    }

    private suspend fun bootstrap(serverSocket: ServerSocketWrapper, scope: CoroutineScope) {
        while (true) {
            val client = serverSocket.accept()

            if(config.isSynchronous && !config.isMultithreaded) {
                handleConnectionSync(client)
            } else {
                handleConnectionAsync(client, scope)
            }
        }
    }

    private suspend fun handleConnectionAsync(socket: ClientSocket, scope: CoroutineScope) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            handleConnectionSync(socket)
        }
    }

    private suspend fun handleConnectionSync(socket: ClientSocket) {
        try {
            while (true) {
                val data = socket.readString()

                if(data.isNullOrEmpty()) {
                    println("Connection closed")

                    break
                }

                when(val event = Events.parseFromClientString(data)) {
                    is Events.ECHO,
                    is Events.UNKNOWN,
                    is Events.TIME,
                    is Events.CLOSE -> {
                        baseController.resolve(socket, event)
                        break;
                    }
                    is Events.DOWNLOAD -> {
                        fileController.download(socket, event)
                    }
                    is Events.UPLOAD -> {
                        fileController.upload(socket, event)
                    }
                }
            }

        } catch (e: Throwable) {
            e.printStackTrace()
            socket.close()
        }
    }
}
