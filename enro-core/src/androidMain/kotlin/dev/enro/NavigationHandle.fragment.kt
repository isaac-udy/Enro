package dev.enro

import androidx.fragment.app.Fragment
import dev.enro.platform.getNavigationKeyInstance
import dev.enro.ui.destinations.fragment.FragmentNavigationHandle
import dev.enro.ui.destinations.fragment.fragmentContextHolder
import kotlin.properties.ReadOnlyProperty

public fun <T : NavigationKey> Fragment.navigationHandle(): ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    return ReadOnlyProperty<Fragment, NavigationHandle<T>> { fragment, _ ->
        val holder = fragment.fragmentContextHolder
        val navigation = holder.navigationHandle
        val delegate = navigation.delegate
        if (delegate is FragmentNavigationHandle.NotInitialized<NavigationKey>) {
            fragment.arguments?.getNavigationKeyInstance()?.let {
                delegate.instance = it
                navigation.instance = it
            }
        }
        @Suppress("UNCHECKED_CAST")
        return@ReadOnlyProperty navigation as NavigationHandle<T>
    }
}
