package dev.enro.ui.decorators

import androidx.core.os.BundleCompat
import androidx.savedstate.SavedState

internal actual fun SavedState.toMap(): Map<String, List<Any?>>? {
    val map = mutableMapOf<String, List<Any?>>()
    this.keySet().forEach { key ->
        val list = BundleCompat.getParcelableArrayList(this, key, Any::class.java).orEmpty()
        map[key] = list
    }
    return map
}
