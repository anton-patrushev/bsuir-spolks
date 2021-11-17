package com.spolks.server.socket

interface ServerSocket {
    suspend fun accept(): ClientSocket
}