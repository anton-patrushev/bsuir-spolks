package com.spolks.common.repository

import com.spolks.common.utils.Chunk
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun writeFile(path: String,
                          fileSize: Long = 0,
                          existingSize: Long = 0,
                          receiveChunk: suspend (ByteArray) -> Int)
    fun readFile(path: String, offset: Long = 0): Flow<Chunk>
    fun getFileSize(path: String): Long
}