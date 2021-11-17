package com.spolks.server.controller

import com.spolks.server.socket.ClientSocket
import com.spolks.common.utils.Events

interface BaseController {
    suspend fun resolve(socket: ClientSocket, event: Events)
}