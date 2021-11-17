package com.spolks.server.di.modules.controller

import com.spolks.server.controller.BaseController
import com.spolks.server.controller.FileController
import com.spolks.server.controller.impl.BaseControllerImpl
import com.spolks.server.controller.impl.FileControllerImpl
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val controllerModule = DI.Module("Controller") {
    bind<BaseController>() with singleton { BaseControllerImpl() }
    bind<FileController>() with singleton { FileControllerImpl(instance()) }
}