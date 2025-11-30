package dev.enro.destination.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

@Composable
@ExperimentalEnroApi
public fun EmbeddedNavigationDestination(
    navigationKey: NavigationKey,
    onClosed: (() -> Unit),
    modifier: Modifier = Modifier,
) {
    val rememberedOnClosed = rememberUpdatedState(onClosed)

    val container = rememberNavigationContainer(
        backstack = backstackOf(navigationKey.asInstance()),
        interceptor = navigationInterceptor {
            onClosed<NavigationKey> {
                if (instance.key != navigationKey) continueWithClose()
                cancelAnd {
                    rememberedOnClosed.value.invoke()
                }
            }
            onCompleted<NavigationKey> {
                if (instance.key != navigationKey) continueWithComplete()
                cancelAnd {
                    rememberedOnClosed.value.invoke()
                }
            }
        }
    )
    Box(modifier = modifier) {
        NavigationDisplay(container)
    }
}

@Composable
@ExperimentalEnroApi
public inline fun <reified T: Any> EmbeddedNavigationDestination(
    navigationKey: NavigationKey.WithResult<T>,
    noinline onClosed: (() -> Unit),
    modifier: Modifier = Modifier,
    noinline onResult: (T) -> Unit = {},
) {
    val rememberedOnClosed = rememberUpdatedState(onClosed)
    val rememberedOnResult = rememberUpdatedState(onResult)

    val container = rememberNavigationContainer(
        backstack = backstackOf(navigationKey.asInstance()),
        interceptor = navigationInterceptor {
            onClosed<NavigationKey> {
                if (instance.key != navigationKey) continueWithClose()
                cancelAnd {
                    rememberedOnClosed.value.invoke()
                }
            }
            onCompleted<NavigationKey.WithResult<T>> {
                if (instance.key != navigationKey) continueWithComplete()
                cancelAnd {
                    rememberedOnResult.value.invoke(result)
                }
            }
        }
    )
    Box(modifier = modifier) {
        NavigationDisplay(container)
    }
}