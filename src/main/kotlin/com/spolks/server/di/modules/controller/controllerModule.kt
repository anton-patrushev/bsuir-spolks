package com.astronaut.server.di.modules.controller

import com.astronaut.server.controller.BaseController
import com.astronaut.server.controller.FileController
import com.astronaut.server.controller.impl.BaseControllerImpl
import com.astronaut.server.controller.impl.FileControllerImpl
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val controllerModule = DI.Module("Controller") {
    bind<BaseController>() with singleton { BaseControllerImpl() }
    bind<FileController>() with singleton { FileControllerImpl(instance()) }
}