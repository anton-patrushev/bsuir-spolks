package com.astronaut.common.utils

fun String.toEvent(): Events {
    return Events.parseFromClientString(this.trim())
}
