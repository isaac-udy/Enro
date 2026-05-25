package dev.enro.ui.destinations

import dev.enro.EnroController
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.asInstance
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

/**
 * Runs the synthetic block bound to this [NavigationDestinationProvider]
 * with a fresh [SyntheticDestinationScope] and returns the [SyntheticOutcome]
 * the block decided on. Returns `null` if this provider isn't a synthetic
 * destination at all.
 *
 * Primarily intended for unit-testing synthetic destinations without going
 * through the navigation container's interceptor pipeline. The
 * `testSyntheticDestination` helpers in enro-test wrap this with default
 * context fixtures and assertion helpers.
 */
/**
 * Looks up the synthetic destination bound to [key] on the controller's
 * registered bindings and runs it via [NavigationDestinationProvider.peekSyntheticOutcome].
 * Returns `null` if no binding exists for [key], or the bound destination
 * isn't a synthetic.
 *
 * Used by enro-test's `testSyntheticDestination` helper to find a synthetic
 * registered on the installed controller without exposing the controller's
 * binding repository as public API.
 */
@AdvancedEnroApi
public fun <K : NavigationKey> EnroController.peekSyntheticOutcome(
    key: K,
    context: NavigationContext,
): SyntheticOutcome? {
    val instance = key.asInstance()
    val binding = runCatching { bindings.bindingFor(instance) }.getOrNull() ?: return null
    @Suppress("UNCHECKED_CAST")
    val provider = binding.provider as NavigationDestinationProvider<K>
    return provider.peekSyntheticOutcome(context, instance)
}

@AdvancedEnroApi
public fun <K : NavigationKey> NavigationDestinationProvider<K>.peekSyntheticOutcome(
    context: NavigationContext,
    instance: NavigationKey.Instance<K>,
): SyntheticOutcome? {
    val syntheticDestination = peekMetadata(instance)[SyntheticDestination.SyntheticDestinationKey]
        ?: return null
    @Suppress("UNCHECKED_CAST")
    val synthetic = syntheticDestination as SyntheticDestination<K>
    val scope = SyntheticDestinationScope(
        context = context,
        instance = instance,
    )
    val thrown = try {
        synthetic.block(scope)
        null
    } catch (outcome: SyntheticDestinationOutcome) {
        outcome
    }
    val effective = thrown ?: scope.finalizeAsSilentCloseIfNoOutcome()

    return when (effective) {
        is SyntheticDestinationOutcome.Open -> SyntheticOutcome.Open(effective.target)
        is SyntheticDestinationOutcome.Close -> SyntheticOutcome.Close(effective.silent)
        is SyntheticDestinationOutcome.Complete -> SyntheticOutcome.Complete(effective.result)
        is SyntheticDestinationOutcome.CompleteFrom -> SyntheticOutcome.CompleteFrom(effective.target)
        is SyntheticDestinationOutcome.SideEffect -> SyntheticOutcome.SideEffect(
            instance = instance,
            block = effective.block,
        )
    }
}
