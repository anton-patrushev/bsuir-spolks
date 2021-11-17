package com.spolks.server.di.modules

import com.spolks.server.di.modules.config.configModule
import com.spolks.server.di.modules.controller.controllerModule
import com.spolks.server.di.modules.repository.repositoryModule
import com.spolks.server.di.modules.server.serverModule
import com.spolks.server.di.modules.service.serviceModule
import com.spolks.server.di.modules.socket.socketModule
import org.kodein.di.DI

val rootModule = DI {
    import(configModule)
    import(socketModule)
    import(repositoryModule)
    import(serviceModule)
    import(controllerModule)
    import(serverModule)
}