package com.astronaut.server

import com.astronaut.server.di.DIRoot
import com.astronaut.server.utils.ServerProtocol

fun main() {
    DIRoot.getConfigInstance().configure(
        isMultithreaded = false,
        isSynchronous = false,
    )

    DIRoot.getServerInstance().start()
}
