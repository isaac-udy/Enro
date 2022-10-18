package dev.enro.core.fragment.container

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.container.NavigationContainer

internal val NavigationContainer.fragmentManager get() = when(parentContext.contextReference) {
    is FragmentActivity -> parentContext.contextReference.supportFragmentManager
    is Fragment -> parentContext.contextReference.childFragmentManager
    else -> throw IllegalStateException("Expected Fragment or FragmentActivity, but was ${parentContext.contextReference}")
}

internal fun NavigationContainer.tryExecutePendingTransitions(): Boolean {
    return kotlin
        .runCatching {
            fragmentManager.executePendingTransactions()
            true
        }
        .getOrDefault(false)
}