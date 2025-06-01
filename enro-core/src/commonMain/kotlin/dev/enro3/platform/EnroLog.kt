package dev.enro3.platform

@PublishedApi
internal expect object EnroLog {
    fun debug(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun error(message: String, throwable: Throwable)
}