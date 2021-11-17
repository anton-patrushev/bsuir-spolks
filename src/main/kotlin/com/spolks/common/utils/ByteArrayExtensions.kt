package com.astronaut.common.utils

fun ByteArray.getUnifiedString(): String {
    val string = String(this)
    return string.trim()
}
