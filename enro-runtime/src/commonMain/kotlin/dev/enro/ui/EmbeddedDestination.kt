package dev.enro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.acceptNone
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.backstackOf
import dev.enro.interceptor.builder.navigationInterceptor

@Composable
@ExperimentalEnroApi
public fun EmbeddedDestination(
    instance: NavigationKey.Instance<NavigationKey>,
    onClosed: () -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rememberedOnClosed = rememberUpdatedState(onClosed)

    val container = rememberNavigationContainer(
        backstack = backstackOf(instance),
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
            onCompleted<NavigationKey> {
                if (this.instance.id != instance.id) continueWithComplete()
                cancelAnd {
                    rememberedOnClosed.value.invoke()
                }
            }
        },
    )
    Box(modifier = modifier) {
        NavigationDisplay(container)
    }
}

@Composable
@ExperimentalEnroApi
public inline fun <reified T : Any> EmbeddedDestination(
    instance: NavigationKey.Instance<NavigationKey.WithResult<T>>,
    noinline onClosed: () -> Unit,
    noinline onCompleted: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rememberedOnClosed = rememberUpdatedState(onClosed)
    val rememberedOnResult = rememberUpdatedState(onCompleted)

    val container = rememberNavigationContainer(
        backstack = backstackOf(instance),
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


