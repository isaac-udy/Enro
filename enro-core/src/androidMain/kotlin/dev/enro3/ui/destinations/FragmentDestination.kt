package dev.enro3.ui.destinations

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro3.EnroController
import dev.enro3.NavigationKey
import dev.enro3.ui.NavigationDestinationProvider
import dev.enro3.ui.NavigationDestinationScope
import dev.enro3.ui.navigationDestination

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

private const val FragmentInstanceKey = "dev.enro3.ui.destinations.FragmentInstanceKey"
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