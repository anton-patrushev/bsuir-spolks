package com.astronaut.server.di.modules.service

import com.astronaut.server.service.FileService
import com.astronaut.server.service.impl.FileServiceImpl
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val serviceModule = DI.Module("Service") {
    bind<FileService>() with singleton { FileServiceImpl(instance()) }
}