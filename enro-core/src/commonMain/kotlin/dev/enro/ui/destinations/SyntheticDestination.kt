package dev.enro.ui.destinations

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination

internal class SyntheticDestination<K : NavigationKey>(
    internal val block: SyntheticDestinationScope<K>.() -> Unit,
) {
    companion object {
        const val SyntheticDestinationKey = "dev.enro.ui.destinations.SyntheticDestinationKey"

        val interceptor = object : NavigationInterceptor() {
            override fun intercept(
                fromContext: NavigationContext,
                containerContext: ContainerContext,
                operation: NavigationOperation.Open<NavigationKey>,
            ): NavigationOperation? {
                val controller = fromContext.controller
                val bindings = controller.bindings.bindingFor(instance = operation.instance)
                val syntheticDestination = bindings.provider.peekMetadata(operation.instance)[SyntheticDestinationKey]
                if (syntheticDestination == null) return operation

                @Suppress("UNCHECKED_CAST")
                val synthetic = requireNotNull(syntheticDestination) as SyntheticDestination<NavigationKey>
                // TODO! Make the synthetic execute in-line during the transition???
                return NavigationOperation.SideEffect{
                    synthetic.block(
                        SyntheticDestinationScope(
                            context = fromContext,
                            instance = operation.instance,
                        )
                    )
                }
            }
        }
    }
}

public fun <K : NavigationKey> syntheticDestination(
    metadata: NavigationDestination.MetadataBuilder<K>.() -> Unit = {},
    block: SyntheticDestinationScope<K>.() -> Unit
) : NavigationDestinationProvider<K> {
    return navigationDestination(
        metadata = {
            metadata.invoke(this)
            add(SyntheticDestination.SyntheticDestinationKey to SyntheticDestination(block))
        }
    ) {
        error("SyntheticDestination with NavigationKey ${navigation.key::class.simpleName} was rendered; SyntheticDestinations should never end up in the Composition. Something is going wrong.")
    }
}
