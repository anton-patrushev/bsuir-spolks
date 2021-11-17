package com.spolks.server.di.modules.config

import com.spolks.server.config.ServerConfig
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val configModule = DI.Module("Config") {
    bind<ServerConfig>() with singleton { ServerConfig() }
}