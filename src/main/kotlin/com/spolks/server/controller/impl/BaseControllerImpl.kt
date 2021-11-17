package com.astronaut.server.controller.impl

import com.astronaut.server.controller.BaseController
import com.astronaut.server.socket.ClientSocket
import com.astronaut.common.utils.Events
import java.text.SimpleDateFormat
import java.util.*

class BaseControllerImpl: BaseController {
    override suspend fun resolve(socket: ClientSocket, event: Events) {
        when(event) {
            is Events.ECHO -> {
                socket.writeString("${event.string}\r\n")
            }
            is Events.TIME -> {
                socket.writeString("${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}\r\n")
            }
            is Events.CLOSE -> {
                socket.close()
            }
            else -> {
                socket.writeString("Unknown command $event\r\n")
            }
        }
    }
}
