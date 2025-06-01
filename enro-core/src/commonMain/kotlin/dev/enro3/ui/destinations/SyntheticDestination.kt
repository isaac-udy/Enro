package dev.enro3.ui.destinations

import dev.enro3.EnroController
import dev.enro3.NavigationKey
import dev.enro3.interceptor.NavigationTransitionInterceptor
import dev.enro3.ui.NavigationDestinationProvider
import dev.enro3.ui.navigationDestination

internal class SyntheticDestination<K : NavigationKey>(
    internal val block: SyntheticDestinationScope<K>.() -> Unit,
) {
    companion object {
        const val SyntheticDestinationKey = "dev.enro3.ui.destinations.SyntheticDestinationKey"

        val interceptor = NavigationTransitionInterceptor { transition ->
            val controller = requireNotNull(EnroController.instance)
            val bindings = transition.opened.map {
                it to controller.bindings.bindingFor(instance = it)
            }
            val synthetics = bindings
                .filter { (instance, binding) ->
                    binding.provider.metadata[SyntheticDestinationKey] != null
                }
                .onEach { (instance, binding) ->
                    @Suppress("UNCHECKED_CAST")
                    val synthetic = requireNotNull(binding.provider.metadata[SyntheticDestinationKey]) as SyntheticDestination<NavigationKey>
                    synthetic.block(SyntheticDestinationScope(instance as NavigationKey.Instance<NavigationKey>))
                }
                .map { (instance, binding) -> instance }

            replaceTransition(
                backstack = transition.targetBackstack - synthetics,
            )
        }
    }
}

public class SyntheticDestinationScope<K : NavigationKey>(
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K = instance.key
}

public fun <K : NavigationKey> syntheticDestination(
    metadata: Map<String, Any> = emptyMap(),
    block: SyntheticDestinationScope<K>.() -> Unit
) : NavigationDestinationProvider<K> {
    return navigationDestination(
        metadata = metadata + (SyntheticDestination.SyntheticDestinationKey to SyntheticDestination(block))
    ) {
        error("Synthetic destinations should not ever end up being rendered")
    }
}
