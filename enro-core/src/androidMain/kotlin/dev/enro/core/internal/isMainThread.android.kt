package dev.enro.core.internal

import android.os.Looper

internal actual fun isMainThread(): Boolean {
    return Looper.myLooper() == Looper.getMainLooper()
}