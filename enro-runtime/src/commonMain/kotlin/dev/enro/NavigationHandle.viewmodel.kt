package dev.enro

import androidx.lifecycle.ViewModel
import dev.enro.viewmodel.NavigationHandleProvider
import dev.enro.viewmodel.navigationHandleReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

/**
 * Delegated property that exposes this ViewModel's [NavigationHandle]
 * typed to [K], with an optional [config] block for lifetime-scoped
 * configuration (e.g. registering an `onCloseRequested` callback).
 *
 * Standard usage:
 * ```
 * class MyViewModel : ViewModel() {
 *     val navigation by navigationHandle<MyKey> {
 *         onCloseRequested {
 *             // ask the user before closing
 *         }
 *     }
 * }
 * ```
 *
 * If [K] doesn't match the destination's actual key type, the property
 * throws on first access with a clear message — usually a sign the wrong
 * ViewModel was wired to the destination. Configuration registered via
 * [config] is torn down when the ViewModel itself is cleared.
 */
public inline fun <reified K : NavigationKey> ViewModel.navigationHandle(
    noinline config: (NavigationHandleConfiguration<K>.() -> Unit)? = null,
): ReadOnlyProperty<ViewModel, NavigationHandle<K>> {
    return navigationHandle(
        K::class,
        config,
    )
}

/**
 * Explicit-[KClass] form of `ViewModel.navigationHandle<K>(config)` for
 * cases where you can't use a reified type parameter.
 */
public fun <K : NavigationKey> ViewModel.navigationHandle(
    keyType: KClass<K>,
    config: (NavigationHandleConfiguration<K>.() -> Unit)? = null,
): ReadOnlyProperty<ViewModel, NavigationHandle<K>> {
    val navigationHandle = getNavigationHandle()
    require(keyType.isInstance(navigationHandle.key)) {
        "The navigation handle key does not match the expected type. Expected ${keyType.simpleName}, but got ${navigationHandle.key::class.simpleName}"
    }

    if (config != null) {
        @Suppress("UNCHECKED_CAST")
        val configuration = NavigationHandleConfiguration(navigationHandle)
            .apply(config as NavigationHandleConfiguration<NavigationKey>.() -> Unit)
        addCloseable(AutoCloseable { configuration.close() })
    }

    @Suppress("UNCHECKED_CAST")
    return ReadOnlyProperty { _, _ -> navigationHandle as NavigationHandle<K> }
}

/**
 * Delegated property that exposes this ViewModel's [NavigationHandle]
 * typed to [K], with no extra configuration. Use the `(config)` overload
 * when you need to register lifetime-scoped behaviour.
 */
public inline fun <reified K : NavigationKey> ViewModel.navigationHandle(): ReadOnlyProperty<ViewModel, NavigationHandle<K>> {
   return navigationHandle(
       config = null,
   )
}

/**
 * Explicit-[KClass] form of `ViewModel.navigationHandle<K>()`.
 */
public fun <K : NavigationKey> ViewModel.navigationHandle(
    keyType: KClass<K>,
): ReadOnlyProperty<ViewModel, NavigationHandle<K>> {
    return navigationHandle(
        keyType = keyType,
        config = null,
    )
}

/**
 * Returns the untyped [NavigationHandle] attached to this ViewModel.
 *
 * Lower-level than the `navigationHandle()` delegated property — use this
 * when you need the handle outside a property declaration (e.g. inside a
 * helper that takes the ViewModel as a parameter). Most destination code
 * should use `by navigationHandle<MyKey>()` instead.
 *
 * Throws if the ViewModel wasn't created with a navigation handle bound
 * to it (i.e. wasn't constructed via the destination's ViewModel factory).
 */
public fun ViewModel.getNavigationHandle(): NavigationHandle<NavigationKey> {
    val reference = navigationHandleReference
    if (reference.navigationHandle == null) {
        reference.navigationHandle = NavigationHandleProvider.get(this::class)
    }
    val navigationHandle = reference.navigationHandle
    requireNotNull(navigationHandle) {
        "Unable to retrieve navigation handle for ViewModel ${this::class.simpleName}"
    }
    return navigationHandle
}
