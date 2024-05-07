package dev.enro.destination.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.core.NavigationKey
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptNone

@Composable
@ExperimentalEnroApi
public fun EmbeddedNavigationDestination(
    navigationKey: NavigationKey.SupportsPush,
    onClosed: (() -> Unit),
    modifier: Modifier = Modifier,
) {
    val rememberedOnClosed = rememberUpdatedState(onClosed)
    val container = rememberNavigationContainer(
        root = navigationKey,
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = acceptNone(),
        interceptor = {
            onClosed<NavigationKey> {
                if (it != navigationKey) return@onClosed continueWithClose()
                rememberedOnClosed.value.invoke()
                cancelClose()
            }
        }
    )
    Box(modifier = modifier) {
        container.Render()
    }
}

@Composable
@ExperimentalEnroApi
public inline fun <reified T: Any> EmbeddedNavigationDestination(
    navigationKey: NavigationKey.SupportsPush.WithResult<T>,
    noinline onClosed: (() -> Unit),
    modifier: Modifier = Modifier,
    noinline onResult: (T) -> Unit = {},
) {
    val rememberedOnClosed = rememberUpdatedState(onClosed)
    val rememberedOnResult = rememberUpdatedState(onResult)

    val container = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.CloseParent,
        root = navigationKey,
        filter = acceptNone(),
        interceptor = {
            onClosed<NavigationKey> {
                if (it != navigationKey) return@onClosed continueWithClose()
                rememberedOnClosed.value.invoke()
                cancelClose()
            }
            onResult<NavigationKey.WithResult<T>, T> { key, result ->
                if (key != navigationKey) return@onResult continueWithClose()
                rememberedOnResult.value.invoke(result)
                cancelResult()
            }
        }
    )
    Box(modifier = modifier) {
        container.Render()
    }
}