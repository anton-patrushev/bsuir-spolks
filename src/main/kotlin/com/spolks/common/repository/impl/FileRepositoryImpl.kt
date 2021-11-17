package com.astronaut.common.repository.impl

import com.astronaut.common.repository.FileRepository
import com.astronaut.common.utils.Chunk
import com.astronaut.common.utils.Events
import kotlinx.coroutines.flow.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


const val CHUNK_SIZE = 16192 //1024
const val DELIMITER = Events.END.STRINGIFIED.toString()
val DELIMITER_ENCODED = DELIMITER.encodeToByteArray()

class FileRepositoryImpl: FileRepository {
    override suspend fun writeFile(path: String,
                                   fileSize: Long,
                                   existingSize: Long,
                                   receiveChunk: suspend (ByteArray) -> Int) {

        val outputStream = FileOutputStream(path, existingSize.toInt() != 0)

        var actualSize: Int
        var commonSize: Long = 0
        val expectedSize = (fileSize - existingSize) + CHUNK_SIZE - (fileSize - existingSize) % CHUNK_SIZE

        println(expectedSize) //4497408

        val start = Date().time

        do {
            val byteArray = ByteArray(CHUNK_SIZE)

            actualSize = receiveChunk(byteArray)

            if(actualSize != -1) {
                commonSize += actualSize

                byteArray.apply {
                    if(commonSize == expectedSize) {
                        val endIndex = ((fileSize + DELIMITER_ENCODED.size) % CHUNK_SIZE).toInt()

                        val end = this.copyOfRange(endIndex - DELIMITER_ENCODED.size, endIndex)
                        val suffix = String(end)

                        if (suffix == DELIMITER) {
                            outputStream.write(
                                this.copyOfRange(0, endIndex - DELIMITER_ENCODED.size),
                                0,
                                endIndex - DELIMITER_ENCODED.size
                            )

                            actualSize = -1
                        }
                    } else {
                        outputStream.write(this, 0, actualSize)
                    }
                }
            } else {
                println(actualSize)
            }
        } while (actualSize != -1)

        println("Received: ${Date().time - start} (${commonSize}B)")

        outputStream.close()
    }

    override fun readFile(path: String, offset: Long) =
        flow<Chunk> {
            FileInputStream(path).use {
                var actualSize = 0
                var commonSize = 0
                val byteArray = ByteArray(CHUNK_SIZE)

                do {
                    actualSize = it.read(byteArray)

                    if(actualSize != -1) {
                        commonSize += actualSize

                        if(commonSize <= offset) {
                            continue
                        }

                        if(actualSize == CHUNK_SIZE) {
                            emit(Chunk(byteArray, false))
                        } else {
                            println("Sent: ${commonSize - actualSize + CHUNK_SIZE}B")

                            emit(Chunk(ByteArray(CHUNK_SIZE) { index ->
                                if (index < actualSize) {
                                    byteArray[index]
                                } else {
                                    if(index >= actualSize && index < actualSize + DELIMITER_ENCODED.size) {
                                        DELIMITER_ENCODED[index - actualSize]
                                    } else {
                                        0
                                    }
                                }
                            }, true))

                            actualSize = -1
                        }
                    } else {
                        println()
                    }

                } while (actualSize != -1)
            }
        }

    override fun getFileSize(path: String): Long {
        val internalPath: Path = Paths.get(path)

        return try {
            Files.size(internalPath)
        } catch (e: IOException) {
            println("No such file: $path")
            0
        }
    }
}
