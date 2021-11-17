package com.astronaut.server.server

import com.astronaut.server.socket.ClientSocket

interface ServerSocketWrapper {
    suspend fun accept(): ClientSocket
}