package com.astronaut.common.utils

import java.nio.ByteBuffer

fun ByteBuffer.toByteArray(): ByteArray {
    val array = ByteArray(remaining())
    get(array)

    return array
}

fun ByteBuffer.copy() {

}
