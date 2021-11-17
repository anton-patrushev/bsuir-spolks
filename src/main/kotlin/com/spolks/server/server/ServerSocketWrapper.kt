package com.spolks.server.server

import com.spolks.server.socket.ClientSocket

interface ServerSocketWrapper {
    suspend fun accept(): ClientSocket
}