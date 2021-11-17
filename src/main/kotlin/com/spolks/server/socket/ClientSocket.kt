package com.astronaut.server.socket

interface ClientSocket {
    suspend fun readString(): String?
    suspend fun readByteArray(data: ByteArray): Int?
    suspend fun writeString(data: String)
    suspend fun writeByteArray(data: ByteArray)
    suspend fun close()
}