package dev.enro.core.internal

public actual inline fun isDebugBuild(): Boolean {
    return dev.enro.core.BuildConfig.DEBUG
}