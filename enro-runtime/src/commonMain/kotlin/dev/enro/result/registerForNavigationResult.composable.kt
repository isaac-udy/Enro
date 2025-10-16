package dev.enro.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import dev.enro.NavigationKey
import dev.enro.ui.LocalNavigationHandle


// TODO this needs much more documentation, it's too complex, maybe a separate file
@Composable
public inline fun <reified R : Any> registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onCompleted: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
): NavigationResultChannel<R> {
    val hashKey = currentCompositeKeyHash
    val navigationHandle = LocalNavigationHandle.current
    val channel = remember(hashKey) {
        NavigationResultChannel<R>(
            id = NavigationResultChannel.Id(
                ownerId = navigationHandle.instance.id,
                resultId = hashKey.toString(),
            ),
            navigationHandle = navigationHandle,
            onClosed = {
                @Suppress("UNCHECKED_CAST")
                this as NavigationResultScope<out NavigationKey.WithResult<out R>>
                onClosed(this)
            },
            onCompleted = {
                @Suppress("UNCHECKED_CAST")
                this as NavigationResultScope<out NavigationKey.WithResult<out R>>
                onCompleted(it)
            }
        )
    }
    LaunchedEffect(hashKey) {
        NavigationResultChannel.observe(this, channel)
    }
    return channel
}

// TODO this needs much more documentation, it's too complex, maybe a separate file
@Composable
public fun registerForNavigationResult(
    onClosed: NavigationResultScope<out NavigationKey>.() -> Unit = {},
    onCompleted: NavigationResultScope<out NavigationKey>.() -> Unit,
): NavigationResultChannel<Unit> {
    val hashKey = currentCompositeKeyHash
    val navigationHandle = LocalNavigationHandle.current
    val channel = remember(hashKey) {
        NavigationResultChannel<Unit>(
            id = NavigationResultChannel.Id(
                ownerId = navigationHandle.instance.id,
                resultId = hashKey.toString(),
            ),
            navigationHandle = navigationHandle,
            onClosed = onClosed,
            onCompleted = {
                onCompleted()
            }
        )
    }
    LaunchedEffect(hashKey) {
        NavigationResultChannel.observe<Unit>(this, channel)
    }
    @Suppress("UNCHECKED_CAST")
    return channel
}