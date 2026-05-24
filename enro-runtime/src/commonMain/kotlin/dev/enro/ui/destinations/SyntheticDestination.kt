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

internal class SyntheticDestination<K : NavigationKey>(
    internal val block: SyntheticDestinationScope<K>.() -> Unit,
) {
    internal companion object {
        internal const val SyntheticDestinationKey = "dev.enro.ui.destinations.SyntheticDestinationKey"

        internal val interceptor = object : NavigationInterceptor() {
            override fun intercept(
                fromContext: NavigationContext,
                containerContext: ContainerContext,
                operation: NavigationOperation.Open<NavigationKey>,
            ): NavigationOperation? {
                if (!isSyntheticDestination(operation.instance)) return operation
                return resolveSyntheticOutcome(
                    fromContext = fromContext,
                    containerContext = containerContext,
                    instance = operation.instance,
                )
            }
        }

        /**
         * Runs the synthetic's block synchronously and converts the outcome
         * into a [NavigationOperation] that takes the place of the original
         * `Open(synthetic)` in the surrounding `processOperations` pass.
         *
         * Pure outcomes (open/close/complete/completeFrom) become the
         * equivalent operation; the side-effect outcome becomes a
         * [NavigationOperation.SideEffect] whose body constructs the
         * [SyntheticSideEffectScope] and invokes the user block in
         * `afterExecution`.
         */
        private fun resolveSyntheticOutcome(
            fromContext: NavigationContext,
            containerContext: ContainerContext,
            instance: NavigationKey.Instance<NavigationKey>,
        ): NavigationOperation {
            val controller = fromContext.controller
            val bindings = controller.bindings.bindingFor(instance = instance)
            val syntheticDestination = bindings.provider.peekMetadata(instance)[SyntheticDestinationKey]
            @Suppress("UNCHECKED_CAST")
            val synthetic = requireNotNull(syntheticDestination) as SyntheticDestination<NavigationKey>
            val scope = SyntheticDestinationScope(
                context = fromContext,
                instance = instance,
            )
            val thrown = try {
                synthetic.block(scope)
                null
            } catch (outcome: SyntheticDestinationOutcome) {
                outcome
            }
            val effectiveOutcome = thrown ?: scope.finalizeAsSilentCloseIfNoOutcome()

            return when (effectiveOutcome) {
                is SyntheticDestinationOutcome.Open ->
                    NavigationOperation.Open(effectiveOutcome.target)
                is SyntheticDestinationOutcome.Close ->
                    NavigationOperation.Close(instance, silent = effectiveOutcome.silent)
                is SyntheticDestinationOutcome.Complete -> when (val result = effectiveOutcome.result) {
                    null -> NavigationOperation.Complete(instance)
                    else -> {
                        @Suppress("UNCHECKED_CAST")
                        NavigationOperation.Complete(
                            instance = instance as NavigationKey.Instance<NavigationKey.WithResult<Any>>,
                            result = result,
                        )
                    }
                }
                is SyntheticDestinationOutcome.CompleteFrom ->
                    NavigationOperation.CompleteFrom(instance, effectiveOutcome.target)
                is SyntheticDestinationOutcome.SideEffect -> NavigationOperation.SideEffect {
                    val sideEffectScope = SyntheticSideEffectScope(
                        context = fromContext,
                        container = containerContext.container,
                        instance = instance,
                    )
                    effectiveOutcome.block(sideEffectScope)
                }
            }
        }
    }
}

public fun <K : NavigationKey> syntheticDestination(
    metadata: NavigationDestination.MetadataBuilder<K>.() -> Unit = {},
    block: SyntheticDestinationScope<K>.() -> Unit,
): NavigationDestinationProvider<K> {
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
    instance: NavigationKey.Instance<*>,
): Boolean {
    return EnroController.instance?.bindings?.bindingFor(instance)
        ?.provider
        ?.peekMetadata(instance)
        ?.contains(SyntheticDestination.SyntheticDestinationKey)
        ?: false
}
