package dev.enro.ui.destinations

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.platform.getNavigationKeyInstance
import dev.enro.platform.putNavigationKeyInstance
import dev.enro.result.NavigationResultChannel
import dev.enro.result.NavigationResultScope
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDestinationScope
import dev.enro.ui.navigationDestination
import kotlin.properties.ReadOnlyProperty

public inline fun <reified T : NavigationKey, reified F : Fragment> fragmentDestination(
    metadata: Map<String, Any> = emptyMap(),
    crossinline arguments: NavigationDestinationScope<T>.() -> Bundle = { Bundle() },
): NavigationDestinationProvider<T> {
    return navigationDestination(metadata) {
        AndroidFragment<F>(
            fragmentState = rememberFragmentState(),
            arguments = arguments().apply {
                putNavigationKeyInstance(navigation.instance)
            },
        ) { fragment ->
        }
    }
}

@PublishedApi
internal fun Fragment.getNavigationKeyInstance(): NavigationKey.Instance<NavigationKey>? {
    return arguments?.getNavigationKeyInstance()
}

public fun <T : NavigationKey> Fragment.navigationHandle() : ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    TODO("FRAGMENT NAV HANDLES")
}

public inline fun <reified R : Any> Fragment.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onCompleted: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
) : ReadOnlyProperty<Fragment, NavigationResultChannel<R>> {
    TODO("FRAGMENT RESULT CHANNELS")
}
