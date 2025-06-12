package dev.enro

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.handle.getNavigationHandleHolder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

public inline fun <reified T : NavigationKey> Fragment.navigationHandle(): ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    return navigationHandle(T::class)
}

public fun <T : NavigationKey> Fragment.navigationHandle(
    keyType: KClass<T>,
): ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    val navigationHandle by lazyNavigationHandle(keyType)
    return ReadOnlyProperty { activity, _ ->
        navigationHandle
    }
}

public inline fun <reified T : NavigationKey> ComponentActivity.navigationHandle(): ReadOnlyProperty<ComponentActivity, NavigationHandle<T>> {
    return navigationHandle(T::class)
}

public fun <T : NavigationKey> ComponentActivity.navigationHandle(
    keyType: KClass<T>,
): ReadOnlyProperty<ComponentActivity, NavigationHandle<T>> {
    val navigationHandle by lazyNavigationHandle(keyType)
    return ReadOnlyProperty { activity, _ ->
        navigationHandle
    }
}

private fun <T, K : NavigationKey> T.lazyNavigationHandle(
    keyType: KClass<K>,
): Lazy<NavigationHandle<K>> where T : LifecycleOwner, T : ViewModelStoreOwner {
    return lazy {
        require(lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            "NavigationHandle can only be accessed after the Activity is in the CREATED state."
        }
        val navigation = getNavigationHandleHolder().navigationHandle
        require(keyType.isInstance(navigation.instance.key)) {
            error("Expected NavigationHandle for ${keyType.qualifiedName}, but found ${navigation.instance.key::class.simpleName}")
        }
        @Suppress("UNCHECKED_CAST")
        return@lazy navigation as NavigationHandle<K>
    }
}