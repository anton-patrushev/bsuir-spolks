package com.spolks.server.server.impl

import com.spolks.server.server.ServerSocketWrapper
import com.spolks.server.socket.ClientSocket
import com.spolks.server.socket.ServerSocket

class ServerSocketWrapperImpl(
    private val socket: ServerSocket
): ServerSocketWrapper {
    override suspend fun accept(): ClientSocket {
        return socket.accept()
    }
}