package com.astronaut.server.controller

import com.astronaut.server.socket.ClientSocket
import com.astronaut.common.utils.Events

interface FileController {
    suspend fun upload(socket: ClientSocket, event: Events.UPLOAD)
    suspend fun download(socket: ClientSocket, event: Events.DOWNLOAD)
}