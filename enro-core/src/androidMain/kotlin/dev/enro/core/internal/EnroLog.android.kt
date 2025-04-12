package dev.enro.core.internal

import android.util.Log

internal actual object EnroLog {
    private const val LOG_TAG = "Enro"

    // Enabled/disabled by EnroTest
    @Suppress("MemberVisibilityCanBePrivate")
    internal var usePrint = false

    actual fun debug(message: String) {
        if (usePrint) {
            // In JVM tests, we don't have a logcat to write to, so we just print to stdout
            println("[Debug] $LOG_TAG: $message")
            return
        }
        Log.d(LOG_TAG, message)
    }

    actual fun warn(message: String) {
        if (usePrint) {
            // In JVM tests, we don't have a logcat to write to, so we just print to stdout
            println("[Warn] $LOG_TAG: $message")
            return
        }
        Log.w(LOG_TAG, message)
    }

    actual fun error(message: String) {
        if (usePrint) {
            // In JVM tests, we don't have a logcat to write to, so we just print to stdout
            println("[Error] $LOG_TAG: $message")
            return
        }
        Log.e(LOG_TAG, message)
    }

    actual fun error(message: String, throwable: Throwable) {
        if (usePrint) {
            // In JVM tests, we don't have a logcat to write to, so we just print to stdout
            println("[Error] $LOG_TAG: $message\n${throwable.stackTraceToString()}")
            return
        }
        Log.e(LOG_TAG, message, throwable)
    }
}