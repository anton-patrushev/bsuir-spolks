package com.astronaut.server.server.impl

import com.astronaut.server.server.ServerSocketWrapper
import com.astronaut.server.socket.ClientSocket
import com.astronaut.server.socket.ServerSocket

class ServerSocketWrapperImpl(
    private val socket: ServerSocket
): ServerSocketWrapper {
    override suspend fun accept(): ClientSocket {
        return socket.accept()
    }
}