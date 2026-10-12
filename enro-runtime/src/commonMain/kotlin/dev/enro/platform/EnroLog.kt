package dev.enro.platform

import dev.enro.annotations.InternalEnroApi

@InternalEnroApi
public expect object EnroLog {
    public fun debug(message: String)
    public fun warn(message: String)
    public fun error(message: String)
    public fun error(message: String, throwable: Throwable)
}