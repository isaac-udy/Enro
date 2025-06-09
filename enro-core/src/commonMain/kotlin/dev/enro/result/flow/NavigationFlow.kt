package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.asInstance
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.NavigationContainerState
import dev.enro.withMetadata
import kotlinx.coroutines.CoroutineScope

@ExperimentalEnroApi
public class NavigationFlow<T> internal constructor(
    internal val reference: NavigationFlowReference,
    private val resultManager: FlowResultManager,
    private val coroutineScope: CoroutineScope,
    internal var flow: NavigationFlowScope.() -> T,
    internal var onCompleted: (T) -> Unit,
) {
    private var steps: List<FlowStep<out Any>> = emptyList()
    public var container: NavigationContainerState? = null
        set(value) {
            field = value
            if (value != null && (steps.isEmpty() || value.backstack.isEmpty())) update()
        }

    internal fun onStepCompleted(step: FlowStep<Any>, result: Any) {
        resultManager.set(step, result)
    }

    internal fun onStepClosed(instance: NavigationKey.Instance<NavigationKey>) {
        val step = instance.metadata.get(FlowStep.MetadataKey)
        if (step == null) return
        resultManager.clear(step)
    }

    /**
     * This method is used to cause the flow to re-evaluate it's current state.
     */
    public fun update() {
        val flowScope = NavigationFlowScope(
            coroutineScope = coroutineScope,
            flow = this,
            resultManager = resultManager,
            navigationFlowReference = reference
        )
        runCatching {
            return@update onCompleted(flowScope.flow())
        }.recover {
            when (it) {
                is NavigationFlowScope.NoResult -> {}
                is NavigationFlowScope.Escape -> return
                else -> throw it
            }
        }
            .getOrThrow()

        val oldSteps = steps
        steps = flowScope.steps
        val container = container ?: return

        val existingInstances = container.backstack
            .mapNotNull { instance ->
                val step = instance.metadata.get(FlowStep.MetadataKey) ?: return@mapNotNull null
                step to instance
            }
            .groupBy { it.first.stepId }
            .mapValues { it.value.lastOrNull() }

        val updatedBackstack = steps
            .filterIndexed { index, flowStep ->
                if (index == steps.lastIndex) return@filterIndexed true
                !flowStep.isTransient
            }
            .map { step ->
                val existingStep = existingInstances[step.stepId]?.second?.takeIf {
                    oldSteps
                        .firstOrNull { it.stepId == step.stepId }
                        ?.dependsOn == step.dependsOn
                }
                existingStep ?: step.key
                    .withMetadata(FlowStep.MetadataKey, step)
                    .withMetadata(NavigationFlowReference.MetadataKey, this)
                    .withMetadata(
                        NavigationResultChannel.ResultIdKey, NavigationResultChannel.Id(
                            ownerId = "NavigationFlow",
                            resultId = step.stepId,
                        )
                    )
                    .asInstance()
                    .apply {
                        metadata.addFrom(step.metadata)
                    }
                    .copy(id = step.stepId)
            }

        container.execute(
            NavigationOperation.AggregateOperation(
                NavigationOperation
                    .SetBackstack(
                        currentBackstack = container.backstack,
                        targetBackstack = updatedBackstack,
                    )
                    .operations
                    .map {
                        when (it) {
                            is NavigationOperation.Close<*> -> it.copy(silent = true)
                            else -> it
                        }
                    }
            )
        )
    }

    @PublishedApi
    internal fun getSteps(): List<FlowStep<out Any>> = steps

    @PublishedApi
    internal fun getResultManager(): FlowResultManager = resultManager

    public companion object {
        internal object ResultFlowKey : NavigationKey.TransientMetadataKey<NavigationFlow<*>?>(null)
        internal object ResultFlowIdKey : NavigationKey.MetadataKey<String?>(null)
    }
}

