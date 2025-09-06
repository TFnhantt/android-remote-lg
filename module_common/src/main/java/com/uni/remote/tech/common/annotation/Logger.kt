package com.uni.remote.tech.common.annotation

fun interface Logger {
    fun log(obj: Any?)

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Generator(
        val name: String = "logger"
    )
}