package dev.enro.core.fragment.container

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.NavigationContext

internal val NavigationContext<*>.fragmentManager get() = when(contextReference) {
    is FragmentActivity -> contextReference.supportFragmentManager
    is Fragment -> contextReference.childFragmentManager
    else -> throw IllegalStateException("Expected Fragment or FragmentActivity, but was $contextReference")
}
