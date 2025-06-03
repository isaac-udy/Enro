package dev.enrolegacy.core.internal

import platform.Foundation.NSThread

internal actual fun isMainThread(): Boolean {
    return NSThread.isMainThread
}