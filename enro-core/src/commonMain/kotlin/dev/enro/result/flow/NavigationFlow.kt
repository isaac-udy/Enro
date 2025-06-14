package dev.enro.result.flow

import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.asInstance
import dev.enro.platform.EnroLog
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.NavigationContainerState
import dev.enro.withMetadata
import kotlinx.coroutines.CoroutineScope

@ExperimentalEnroApi
public class NavigationFlow<T> internal constructor(
    internal val reference: NavigationFlowReference,
    private val navigationHandle: NavigationHandle<*>,
    private val coroutineScope: CoroutineScope,
    internal var flow: NavigationFlowScope.() -> T,
    internal var onCompleted: (T) -> Unit,
) {
    private var steps: List<FlowStep<Any>> = emptyList()

    private val resultManager = FlowResultManager.create(navigationHandle)

    public var container: NavigationContainerState? = null
        set(value) {
            if (field == value) return
            field = value
            if (value == null) return
            update(fromContainerChange = true)
        }

    internal fun onStepCompleted(stepId: String, result: Any) {
        val step = steps.firstOrNull { it.stepId == stepId }
        if (step == null) {
            EnroLog.error("Received result for id $stepId, but no active steps had that id")
        }
        step as FlowStep<Any>
        resultManager.set(step, result)
    }

    internal fun onStepClosed(stepId: String) {
        resultManager.clear(stepId)
    }

    /**
     * This method is used to cause the flow to re-evaluate it's current state.
     */
    public fun update() {
        update(
            fromContainerChange = false
        )
    }

    private fun update(
        fromContainerChange: Boolean
    ) {
        val flowScope = NavigationFlowScope(
            coroutineScope = coroutineScope,
            flow = this,
            resultManager = resultManager,
            navigationFlowReference = reference
        )
        val result = runCatching {
            flowScope.flow()
        }.recover {
            when (it) {
                is NavigationFlowScope.NoResult -> null
                is NavigationFlowScope.Escape -> return
                else -> throw it
            }
        }.getOrThrow()

        val oldSteps = steps
        steps = flowScope.steps
        val container = container ?: return

        val existingInstances = container.backstack
            .mapNotNull { instance ->
                val step = instance.metadata.get(FlowStep.FlowStepIdKey) ?: return@mapNotNull null
                step to instance
            }
            .groupBy { it.first }
            .mapValues { it.value.lastOrNull() }

        if (fromContainerChange && existingInstances.isNotEmpty()) {
            // If the update is being caused by a container change, that might mean the NavigationFlow
            // is being restored from a saved state. If we're being restored from a saved state,
            // we don't actually want to change what's in the backstack, we just want to make sure
            // that the steps list is up to date, so we can return here after the steps list is updated
            return
        }
        if (result != null) {
            onCompleted(result)
            return
        }

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
                    .withMetadata(FlowStep.FlowStepIdKey, step.stepId)
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

