package com.astronaut.server.controller.impl

import com.astronaut.server.controller.FileController
import com.astronaut.server.service.FileService
import com.astronaut.server.socket.ClientSocket
import com.astronaut.common.utils.Events
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion

class FileControllerImpl(
    private val fileService: FileService
): FileController {
    override suspend fun upload(socket: ClientSocket, event: Events.UPLOAD) {
        val path = "data/server/${event.filename}"

        val size = fileService.getFileSize(path);

        socket.writeString(Events.OK(size).toString())

        when(val command = Events.parseFromClientString(socket.readString() ?: "")) {
            is Events.START -> {
                println("Starting transmission...")
            }
            is Events.END -> {
                println("File already uploaded")
                return
            }
            else -> {
                println("Unexpected command: $command")
            }
        }

        fileService.writeFile(path, event.size, size) {
            socket.readByteArray(it) ?: -1
        }
    }

    override suspend fun download(socket: ClientSocket, event: Events.DOWNLOAD) {
        val path = "data/server/${event.filename}"

        val size = fileService.getFileSize(path);

        socket.writeString(Events.OK(size).toString())

        if(event.size == size) {
            println("File was already downloaded!")
            return
        }

        fileService.readFile(path, event.size)
            .catch {
                socket.writeString(Events.ERROR("No such file: ${event.filename}").toString())
            }
            .collect {
                socket.writeByteArray(it.data)
            }
    }
}
