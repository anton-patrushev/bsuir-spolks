package com.spolks.server.controller

import com.spolks.server.socket.ClientSocket
import com.spolks.common.utils.Events

interface FileController {
    suspend fun upload(socket: ClientSocket, event: Events.UPLOAD)
    suspend fun download(socket: ClientSocket, event: Events.DOWNLOAD)
}