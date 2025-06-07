package dev.enro.ui.destinations

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination

internal class SyntheticDestination<K : NavigationKey>(
    internal val block: SyntheticDestinationScope<K>.() -> Unit,
) {
    companion object {
        const val SyntheticDestinationKey = "dev.enro.ui.destinations.SyntheticDestinationKey"

        val interceptor = object : NavigationInterceptor() {
            override fun intercept(
                context: NavigationContext,
                operation: NavigationOperation.Open<NavigationKey>,
            ): NavigationOperation? {
                val controller = context.controller
                val bindings = controller.bindings.bindingFor(instance = operation.instance)
                if (bindings.provider.metadata[SyntheticDestinationKey] == null) return operation

                @Suppress("UNCHECKED_CAST")
                val synthetic = requireNotNull(bindings.provider.metadata[SyntheticDestinationKey]) as SyntheticDestination<NavigationKey>
                // TODO! Make the synthetic execute in-line during the transition???
                return NavigationOperation.SideEffect{
                    synthetic.block(
                        SyntheticDestinationScope(
                            context = context,
                            instance = operation.instance,
                        )
                    )
                }
            }
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
