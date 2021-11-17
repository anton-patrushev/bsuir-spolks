package com.spolks.server

import com.spolks.server.di.DIRoot

fun main() {
    DIRoot.getConfigInstance().configure(
        isMultithreaded = false,
        isSynchronous = false,
    )

    DIRoot.getServerInstance().start()
}
