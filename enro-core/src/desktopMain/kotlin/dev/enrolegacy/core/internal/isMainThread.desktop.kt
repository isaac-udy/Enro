package dev.enrolegacy.core.internal

import javax.swing.SwingUtilities

internal actual fun isMainThread(): Boolean {
    return SwingUtilities.isEventDispatchThread()
}