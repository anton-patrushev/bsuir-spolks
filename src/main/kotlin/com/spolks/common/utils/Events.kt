package com.spolks.common.utils

sealed class Events {
    data class ECHO(val string: String): Events() {
        override fun toString(): String = "ECHO $string\n"
    }

    object TIME : Events() {
        override fun toString(): String = "TIME\n"
    }

    object CLOSE : Events() {
        override fun toString(): String = "CLOSE\n"
    }

    data class DOWNLOAD(val filename: String, val size: Long): Events() {
        override fun toString(): String = "DOWNLOAD $filename $size\n"
    }

    data class UPLOAD(val filename: String, val size: Long): Events() {
        override fun toString(): String = "UPLOAD $filename $size\n"
    }

    data class UNKNOWN(val data: String): Events() {
        override fun toString(): String = "UNKNOWN $data\n"
    }

    data class OK(val payload: Long): Events() {
        override fun toString(): String = "OK $payload\n"
    }

    data class ERROR(val reason: String): Events() {
        override fun toString(): String = "ERROR $reason\n"
    }

    class START : Events() {
        companion object {
            const val STRINGIFIED = "START"
        }
        override fun toString(): String = "$STRINGIFIED\n"
    }

    class END : Events() {
        companion object {
            const val STRINGIFIED = "END"
        }
        override fun toString(): String = "$STRINGIFIED\n"
    }

    // UDP WINDOW MODE ONLY
    data class APPROVE(val payload: Long): Events() {
        override fun toString(): String = "APPROVE $payload\n"
    }

    data class RETRY(val payload: Long): Events() {
        override fun toString(): String = "RETRY $payload\n"
    }
    //

    companion object {
        fun parseFromClientString(data: String): Events =
            when {
                data.startsWith("ECHO ") -> {
                    ECHO(data.substring(5, data.length))
                }
                data.startsWith("TIME") -> {
                    TIME
                }
                data.startsWith("CLOSE") -> {
                    CLOSE
                }
                data.startsWith("DOWNLOAD ") -> {
                    val parsed = data.split(" ")

                    DOWNLOAD(parsed[1], parsed[2].toLong())
                }
                data.startsWith("UPLOAD ") -> {
                    val parsed = data.split(" ")

                    UPLOAD(parsed[1], parsed[2].toLong())
                }
                data.startsWith("OK ") -> {
                    OK(data.substring(3, data.length).toLong())
                }
                data.startsWith("ERROR ") -> {
                    ERROR(data.substring(6, data.length))
                }
                data.startsWith("START") -> {
                    START()
                }
                data.startsWith("END") -> {
                    END()
                }
                data.startsWith("APPROVE ") -> {
                    APPROVE(data.substring(8, data.length).toLong())
                }
                data.startsWith("RETRY ") -> {
                    RETRY(data.substring(6, data.length).toLong())
                }
                else -> {
                    UNKNOWN(data)
                }
            }
    }
}