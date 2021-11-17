package com.astronaut.server.controller

import com.astronaut.server.socket.ClientSocket
import com.astronaut.common.utils.Events

interface BaseController {
    suspend fun resolve(socket: ClientSocket, event: Events)
}