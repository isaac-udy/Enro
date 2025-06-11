package dev.enro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.acceptNone
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.interceptor.builder.navigationInterceptor

@Composable
@ExperimentalEnroApi
public fun EmbeddedDestination(
    instance: NavigationKey.Instance<NavigationKey>,
    onClosed: (() -> Unit),
    modifier: Modifier = Modifier,
) {
    val rememberedOnClosed = rememberUpdatedState(onClosed)

    val container = rememberNavigationContainer(
        backstack = listOf(instance),
        interceptor = navigationInterceptor {
            onOpened<NavigationKey> {
                cancel()
            }
            onClosed<NavigationKey> {
                if (this.instance.id != instance.id) continueWithClose()
                cancelAnd {
                    rememberedOnClosed.value.invoke()
                }
            }
            onCompleted<NavigationKey> {
                if (this.instance.id != instance.id) continueWithComplete()
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
public inline fun <reified T: Any> EmbeddedDestination(
    instance: NavigationKey.Instance<NavigationKey.WithResult<T>>,
    noinline onClosed: (() -> Unit),
    modifier: Modifier = Modifier,
    noinline onResult: (T) -> Unit = {},
) {
    val rememberedOnClosed = rememberUpdatedState(onClosed)
    val rememberedOnResult = rememberUpdatedState(onResult)

    val container = rememberNavigationContainer(
        backstack = listOf(instance),
        filter = acceptNone(),
        interceptor = navigationInterceptor {
            onOpened<NavigationKey> {
                cancel()
            }
            onClosed<NavigationKey> {
                if (this.instance.id != instance.id) continueWithClose()
                cancelAnd {
                    rememberedOnClosed.value.invoke()
                }
            }
            onCompleted<NavigationKey.WithResult<T>> {
                if (this.instance.id != instance.id) continueWithComplete()
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