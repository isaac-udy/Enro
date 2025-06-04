package dev.enro.ui.destinations

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
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
                addNavigationKeyInstance(navigation.instance)
            },
        ) { fragment ->
        }
    }
}

private const val FragmentInstanceKey = "dev.enro.ui.destinations.FragmentInstanceKey"
@PublishedApi
internal fun Bundle.addNavigationKeyInstance(instance: NavigationKey.Instance<out NavigationKey>) {
    val savedStateConfig = requireNotNull(EnroController.instance).serializers.savedStateConfiguration
    val encodedInstance = encodeToSavedState(instance, savedStateConfig)
    putBundle(FragmentInstanceKey, encodedInstance)
}

@PublishedApi
internal fun Fragment.getNavigationKeyInstance(): NavigationKey.Instance<NavigationKey>? {
    return arguments?.getBundle(FragmentInstanceKey)?.let { encodedInstance ->
        val savedStateConfig = requireNotNull(EnroController.instance).serializers.savedStateConfiguration
        decodeFromSavedState(encodedInstance, savedStateConfig)
    }
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
