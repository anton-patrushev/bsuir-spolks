package com.spolks.server.di

import com.spolks.server.config.ServerConfig
import com.spolks.server.di.modules.rootModule
import com.spolks.server.server.Server
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

object DIRoot: DIAware {
    override val di: DI
        get() = rootModule

    private inline fun <reified T> instance(): T {
        val instance: T by di.instance()

        return instance
    }

    fun getServerInstance(): Server {
        return instance()
    }

    fun getConfigInstance(): ServerConfig {
        return instance()
    }
}