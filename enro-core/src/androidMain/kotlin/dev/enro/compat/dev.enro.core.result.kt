package dev.enro.core.result

import androidx.fragment.app.Fragment
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.core.asPresent
import dev.enro.core.asPush
import dev.enro.core.compose.NavigationResultChannelCompat
import dev.enro.result.NavigationResultScope
import kotlin.properties.ReadOnlyProperty
import dev.enro.ui.destinations.registerForNavigationResult as fragmentRegisterForNavigationResult

public fun <R: Any> NavigationHandle<out NavigationKey.WithResult<R>>.deliverResultFromPush(
    key: dev.enro.core.NavigationKey.SupportsPush.WithResult<R>,
) {
    execute(NavigationOperation.completeFrom(instance, key.asPush()))
}

public fun <R: Any> NavigationHandle<out NavigationKey.WithResult<R>>.deliverResultFromPresent(
    key: dev.enro.core.NavigationKey.SupportsPresent.WithResult<R>
) {
    execute(NavigationOperation.completeFrom(instance, key.asPresent()))
}


public inline fun <reified R : Any> Fragment.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onCompleted: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
) : ReadOnlyProperty<Fragment, NavigationResultChannelCompat<R>> {
    return ReadOnlyProperty { fragment, prop ->
        val channel = fragmentRegisterForNavigationResult(onClosed, onCompleted).getValue(fragment, prop)
        NavigationResultChannelCompat(channel)
    }
}
