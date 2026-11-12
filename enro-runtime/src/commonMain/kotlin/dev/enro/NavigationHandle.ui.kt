package dev.enro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.handle.NavigationHandleHolder
import dev.enro.ui.LocalNavigationContext
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/**
 * Returns the untyped [NavigationHandle] of the destination that contains
 * this Composable. `navigationHandle<MyKey>()` returns a typed
 * [NavigationHandle].
 *
 * Throws when called outside the content of a destination.
 */
@JvmName("untypedNavigationHandle")
@Composable
public fun navigationHandle(): NavigationHandle<NavigationKey> {
    return navigationHandle<NavigationKey>()
}

/**
 * Returns the [NavigationHandle] of the destination that contains this
 * Composable, typed to [T].
 *
 * Throws if the key of the destination is not a [T], or when called
 * outside the content of a destination.
 */
@Composable
public inline fun <reified T: NavigationKey> navigationHandle(): NavigationHandle<T> {
    return navigationHandle(T::class)
}

/**
 * `navigationHandle<T>()` with the key type passed as a [KClass].
 */
@Composable
public fun <T: NavigationKey> navigationHandle(
    keyType: KClass<T>,
): NavigationHandle<T> {
    val holder = viewModel<NavigationHandleHolder<T>>(
        viewModelStoreOwner = LocalNavigationContext.current,
    ) {
        error("No NavigationHandle found for ${keyType.displayName}")
    }
    val navigationHandle = holder.navigationHandle
    @Suppress("USELESS_IS_CHECK")
    require(navigationHandle.instance.key is T) {
        "Expected key of type ${keyType.displayName}, but found ${navigationHandle.instance.key::class}"
    }
    return navigationHandle
}

/**
 * Applies [block] to this handle while the calling Composable is in the
 * composition. The registrations made in [block] (for example
 * `onCloseRequested { … }`) are removed when the Composable leaves the
 * composition.
 *
 * [configure] runs in a [DisposableEffect] keyed on [block]: a new [block]
 * instance removes the previous registrations and applies the new [block].
 */
@Composable
public inline fun <reified T: NavigationKey> NavigationHandle<T>.configure(
    noinline block: NavigationHandleConfiguration<T>.() -> Unit
) {
    DisposableEffect(block) {
        val configuration = NavigationHandleConfiguration(this@configure).apply(block)
        onDispose { configuration.close() }
    }
}
