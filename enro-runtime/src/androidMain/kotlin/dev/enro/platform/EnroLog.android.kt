package dev.enro.platform

import android.app.Application
import android.util.Log
import dev.enro.EnroController
import dev.enro.annotations.InternalEnroApi

@InternalEnroApi
public actual object EnroLog {
    private const val LOG_TAG = "Enro"

    // Set by generated installNavigationController functions; the name must stay stable.
    public var forceAndroidLogs: Boolean = false

    // True when no controller is installed with an Application, as in a JVM test. A JVM test has
    // no logcat, so EnroLog prints to stdout.
    @Suppress("MemberVisibilityCanBePrivate")
    internal val usePrint
        get() = !forceAndroidLogs && EnroController.instance?.platformReference !is Application

    public actual fun debug(message: String) {
        if (usePrint) {
            println("[Debug] $LOG_TAG: $message")
            return
        }
        Log.d(LOG_TAG, message)
    }

    public actual fun warn(message: String) {
        if (usePrint) {
            println("[Warn] $LOG_TAG: $message")
            return
        }
        Log.w(LOG_TAG, message)
    }

    public actual fun error(message: String) {
        if (usePrint) {
            println("[Error] $LOG_TAG: $message")
            return
        }
        Log.e(LOG_TAG, message)
    }

    public actual fun error(message: String, throwable: Throwable) {
        if (usePrint) {
            println("[Error] $LOG_TAG: $message\n${throwable.stackTraceToString()}")
            return
        }
        Log.e(LOG_TAG, message, throwable)
    }
}