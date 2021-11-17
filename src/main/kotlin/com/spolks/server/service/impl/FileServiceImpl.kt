package com.spolks.server.service.impl

import com.spolks.common.repository.FileRepository
import com.spolks.common.utils.Chunk
import com.spolks.server.service.FileService
import kotlinx.coroutines.flow.Flow

class FileServiceImpl(
    private val fileRepository: FileRepository
): FileService {
    override suspend fun writeFile(
        path: String,
        fileSize: Long,
        existingSize: Long,
        receiveChunk: suspend (ByteArray) -> Int
    ) = fileRepository.writeFile(path, fileSize, existingSize, receiveChunk)

    override fun readFile(path: String, offset: Long): Flow<Chunk> = fileRepository.readFile(path, offset)

    override fun getFileSize(path: String): Long = fileRepository.getFileSize(path)
}