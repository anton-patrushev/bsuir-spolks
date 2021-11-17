package com.astronaut.server.di.modules

import com.astronaut.server.di.modules.config.configModule
import com.astronaut.server.di.modules.controller.controllerModule
import com.astronaut.server.di.modules.repository.repositoryModule
import com.astronaut.server.di.modules.server.serverModule
import com.astronaut.server.di.modules.service.serviceModule
import com.astronaut.server.di.modules.socket.socketModule
import org.kodein.di.DI

val rootModule = DI {
    import(configModule)
    import(socketModule)
    import(repositoryModule)
    import(serviceModule)
    import(controllerModule)
    import(serverModule)
}