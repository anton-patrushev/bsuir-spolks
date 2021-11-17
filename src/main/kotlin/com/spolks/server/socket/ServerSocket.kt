package com.astronaut.server.socket

interface ServerSocket {
    suspend fun accept(): ClientSocket
}