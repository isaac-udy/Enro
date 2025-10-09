package dev.enro.ui.destinations

import dev.enro.EnroController
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination

public class SyntheticDestination<K : NavigationKey>(
    internal val block: SyntheticDestinationScope<K>.() -> Unit,
) {
    public companion object {
        internal const val SyntheticDestinationKey = "dev.enro.ui.destinations.SyntheticDestinationKey"

        internal val interceptor = object : NavigationInterceptor() {
            override fun intercept(
                fromContext: NavigationContext,
                containerContext: ContainerContext,
                operation: NavigationOperation.Open<NavigationKey>,
            ): NavigationOperation? {
                if (!isSyntheticDestination(operation.instance)) return operation
                return NavigationOperation.SideEffect{
                    executeSynthetic(
                        fromContext = fromContext,
                        instance = operation.instance
                    )
                }
            }
        }

        public fun executeSynthetic(
            fromContext: NavigationContext,
            instance: NavigationKey.Instance<NavigationKey>,
        ) {
            val controller = fromContext.controller
            val bindings = controller.bindings.bindingFor(instance = instance)
            val syntheticDestination = bindings.provider.peekMetadata(instance)[SyntheticDestinationKey]
            @Suppress("UNCHECKED_CAST")
            val synthetic = requireNotNull(syntheticDestination) as SyntheticDestination<NavigationKey>
            synthetic.block(
                SyntheticDestinationScope(
                    context = fromContext,
                    instance = instance,
                )
            )
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

public fun isSyntheticDestination(
    instance: NavigationKey.Instance<*>
): Boolean {
    return EnroController.instance?.bindings?.bindingFor(instance)
        ?.provider
        ?.peekMetadata(instance)
        ?.contains(SyntheticDestination.SyntheticDestinationKey)
        ?: false
}

