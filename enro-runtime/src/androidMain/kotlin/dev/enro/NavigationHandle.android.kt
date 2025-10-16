package dev.enro

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import dev.enro.handle.getNavigationHandleHolder
import dev.enro.platform.getNavigationKeyInstance
import dev.enro.ui.destinations.fragment.FragmentNavigationHandle
import dev.enro.ui.destinations.fragment.fragmentContextHolder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

public inline fun <reified T : NavigationKey> Fragment.navigationHandle(): ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    return navigationHandle(T::class)
}

public fun <T : NavigationKey> Fragment.navigationHandle(
    keyType: KClass<T>,
): ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    return ReadOnlyProperty<Fragment, NavigationHandle<T>> { fragment, _ ->
        require(lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            "NavigationHandle can only be accessed after the Activity is in the CREATED state."
        }
        val holder = fragment.fragmentContextHolder
        val navigation = holder.navigationHandle
        val delegate = navigation.delegate
        if (delegate is FragmentNavigationHandle.NotInitialized<NavigationKey>) {
            fragment.arguments?.getNavigationKeyInstance()?.let {
                delegate.instance = it
                navigation.instance = it
            }
        }
        require(keyType.isInstance(navigation.instance.key)) {
            error("Expected NavigationHandle for ${keyType.qualifiedName}, but found ${navigation.instance.key::class.simpleName}")
        }
        @Suppress("UNCHECKED_CAST")
        return@ReadOnlyProperty navigation as NavigationHandle<T>
    }
}

public inline fun <reified T : NavigationKey> ComponentActivity.navigationHandle(): ReadOnlyProperty<ComponentActivity, NavigationHandle<T>> {
    return navigationHandle(T::class)
}

public fun <T : NavigationKey> ComponentActivity.navigationHandle(
    keyType: KClass<T>,
): ReadOnlyProperty<ComponentActivity, NavigationHandle<T>> {
    val navigationHandle by lazy {
        require(lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            "NavigationHandle can only be accessed after the Activity is in the CREATED state."
        }
        val navigation = getNavigationHandleHolder().navigationHandle
        require(keyType.isInstance(navigation.instance.key)) {
            error("Expected NavigationHandle for ${keyType.qualifiedName}, but found ${navigation.instance.key::class.simpleName}")
        }
        @Suppress("UNCHECKED_CAST")
        return@lazy navigation as NavigationHandle<T>
    }
    return ReadOnlyProperty { activity, _ ->
        navigationHandle
    }
}