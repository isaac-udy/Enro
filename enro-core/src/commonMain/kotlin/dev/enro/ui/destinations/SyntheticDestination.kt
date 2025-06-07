package dev.enro.ui.destinations

import dev.enro.EnroController
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext
import dev.enro.interceptor.NavigationTransitionInterceptor
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination

internal class SyntheticDestination<K : NavigationKey>(
    internal val block: SyntheticDestinationScope<K>.() -> Unit,
) {
    companion object {
        const val SyntheticDestinationKey = "dev.enro.ui.destinations.SyntheticDestinationKey"

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
                    synthetic.block(
                        SyntheticDestinationScope(
                            context = context,
                            instance = instance,
                        )
                    )
                }
                .map { (instance, binding) -> instance }

            replaceTransition(
                backstack = transition.targetBackstack - synthetics,
            )
        }
    }
}

public class SyntheticDestinationScope<K : NavigationKey>(
    public val context: NavigationContext,
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K = instance.key

    public val destinationContext: DestinationContext<NavigationKey>?
        get() = when(context) {
            is DestinationContext<*> -> context
            is ContainerContext -> context.activeChild
            is RootContext -> context.activeChild?.activeChild
        }
}

public fun <K : NavigationKey> syntheticDestination(
    metadata: Map<String, Any> = emptyMap(),
    block: SyntheticDestinationScope<K>.() -> Unit
) : NavigationDestinationProvider<K> {
    return navigationDestination(
        metadata = metadata + (SyntheticDestination.SyntheticDestinationKey to SyntheticDestination(block))
    ) {
        error("SyntheticDestination with NavigationKey ${navigation.key::class.simpleName} was rendered; SyntheticDestinations should never end up in the Composition. Something is going wrong.")
    }
}
