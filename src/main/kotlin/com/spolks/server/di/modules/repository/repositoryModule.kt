package com.spolks.server.di.modules.repository

import com.spolks.common.repository.FileRepository
import com.spolks.common.repository.impl.FileRepositoryImpl
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val repositoryModule = DI.Module("Repository") {
    bind<FileRepository>() with singleton { FileRepositoryImpl() }
}