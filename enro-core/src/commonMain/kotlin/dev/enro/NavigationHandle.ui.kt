package dev.enro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.handle.NavigationHandleHolder
import dev.enro.ui.LocalNavigationContext
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@JvmName("untypedNavigationHandle")
@Composable
public fun navigationHandle(): NavigationHandle<out NavigationKey> {
    return navigationHandle<NavigationKey>()
}

@Composable
public inline fun <reified T: NavigationKey> navigationHandle(): NavigationHandle<T> {
    return navigationHandle(T::class)
}

@Composable
public fun <T: NavigationKey> navigationHandle(
    keyType: KClass<T>,
): NavigationHandle<T> {
    val holder = viewModel<NavigationHandleHolder<T>>(
        viewModelStoreOwner = LocalNavigationContext.current,
    ) {
        error("No NavigationHandle found for ${keyType::class}")
    }
    val navigationHandle = holder.navigationHandle
    @Suppress("USELESS_IS_CHECK")
    require(navigationHandle.instance.key is T) {
        "Expected key of type ${keyType::class}, but found ${navigationHandle.instance.key::class}"
    }
    return navigationHandle
}

@Composable
public inline fun <reified T: NavigationKey> NavigationHandle<T>.configure(
    noinline block: NavigationHandleConfiguration<T>.() -> Unit
) {
    DisposableEffect(block) {
        val configuration = NavigationHandleConfiguration(this@configure).apply(block)
        onDispose { configuration.close() }
    }
}
