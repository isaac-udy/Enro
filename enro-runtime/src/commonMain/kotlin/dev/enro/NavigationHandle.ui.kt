package dev.enro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.handle.NavigationHandleHolder
import dev.enro.ui.LocalNavigationContext
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/**
 * Returns the [NavigationHandle] for the destination this Composable is
 * rendering in. Untyped — the handle is `NavigationHandle<NavigationKey>`,
 * so the key type is whatever the destination was opened with.
 *
 * Prefer the reified `navigationHandle<MyKey>()` overload when you know
 * the key type, to avoid casting and to surface mismatches at compile
 * time.
 *
 * Throws if called outside a destination composition (i.e. outside the
 * Composable lambda passed to `navigationDestination<…>`).
 */
@JvmName("untypedNavigationHandle")
@Composable
public fun navigationHandle(): NavigationHandle<NavigationKey> {
    return navigationHandle<NavigationKey>()
}

/**
 * Returns the [NavigationHandle] typed to [T] for the destination this
 * Composable is rendering in. Verifies at runtime that the destination's
 * key really is a [T] and throws a descriptive error otherwise — usually
 * a sign the function was called from the wrong destination.
 *
 * The typed handle gives you access to the result-aware overloads of
 * [complete] / [completeFrom] / [closeAndCompleteFrom] when [T] extends
 * [NavigationKey.WithResult].
 */
@Composable
public inline fun <reified T: NavigationKey> navigationHandle(): NavigationHandle<T> {
    return navigationHandle(T::class)
}

/**
 * Explicit-[KClass] variant of `navigationHandle<T>()` for the rare case
 * you can't use a reified type parameter (e.g. dynamically chosen key
 * type, Java interop).
 */
@Composable
public fun <T: NavigationKey> navigationHandle(
    keyType: KClass<T>,
): NavigationHandle<T> {
    val holder = viewModel<NavigationHandleHolder<T>>(
        viewModelStoreOwner = LocalNavigationContext.current,
    ) {
        error("No NavigationHandle found for ${keyType.qualifiedName}")
    }
    val navigationHandle = holder.navigationHandle
    @Suppress("USELESS_IS_CHECK")
    require(navigationHandle.instance.key is T) {
        "Expected key of type ${keyType.qualifiedName}, but found ${navigationHandle.instance.key::class}"
    }
    return navigationHandle
}

/**
 * Applies a [NavigationHandleConfiguration] block to this handle for the
 * lifetime of the surrounding composition. Registrations made inside
 * [block] (e.g. `onCloseRequested { … }`) are torn down automatically
 * when the Composable leaves composition.
 *
 * Backed by a [DisposableEffect] keyed on [block], so changing the block
 * identity tears down the previous configuration and re-runs the new one.
 * For a `ViewModel`-scoped lifetime, use the `config` parameter on the
 * `ViewModel.navigationHandle` delegated property instead.
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
