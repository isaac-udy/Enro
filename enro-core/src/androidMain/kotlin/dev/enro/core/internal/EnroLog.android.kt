package dev.enro.core.internal

import android.util.Log

internal actual object EnroLog {
    private const val LOG_TAG = "Enro"

    actual fun debug(message: String) {
        Log.d(LOG_TAG, message)
    }

    actual fun warn(message: String) {
        Log.w(LOG_TAG, message)
    }

    actual fun error(message: String) {
        Log.e(LOG_TAG, message)
    }

    actual fun error(message: String, throwable: Throwable) {
        Log.e(LOG_TAG, message, throwable)
    }
}