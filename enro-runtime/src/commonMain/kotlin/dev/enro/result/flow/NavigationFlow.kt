package dev.enro.result.flow

import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asBackstack
import dev.enro.asInstance
import dev.enro.platform.EnroLog
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.NavigationContainerState
import dev.enro.withMetadata
import kotlinx.coroutines.CoroutineScope

public class NavigationFlow<T> internal constructor(
    internal val reference: NavigationFlowReference,
    private val navigationHandle: NavigationHandle<*>,
    private val coroutineScope: CoroutineScope,
    internal var flow: NavigationFlowScope.() -> T,
    internal var onCompleted: (T) -> Unit,
) {
    private var steps: List<FlowStep<Any>> = emptyList()

    private val resultManager = FlowResultManager.create(navigationHandle)

    internal var container: NavigationContainerState? = null
        set(value) {
            if (field == value) return
            field = value
            if (value == null) return
            update(fromContainerChange = true)
        }

    internal fun onStepCompleted(id: FlowStep.Id<*>, result: Any) {
        val step = steps.firstOrNull { it.id == id }
        if (step == null) {
            EnroLog.error("Received result for id ${id.value}, but no active steps had that id")
        }
        step as FlowStep<Any>
        resultManager.set(step, result)
    }

    internal fun onStepClosed(id: FlowStep.Id<*>) {
        resultManager.clear(id)
    }

    /**
     * Runs the flow lambda again. When the lambda returns a result, [update] calls `onCompleted`.
     * Otherwise [update] sets the backstack of the flow's container to the steps the lambda declared.
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
                val step = instance.flowStepId ?: return@mapNotNull null
                step to instance
            }
            .groupBy { it.first }
            .mapValues { it.value.lastOrNull() }

        if (fromContainerChange && existingInstances.isNotEmpty()) {
            // A new container that already contains flow steps was restored from saved state.
            // The restored backstack is kept; only `steps` is updated.
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
                val existingStep = existingInstances[step.id]?.second?.takeIf {
                    oldSteps
                        .firstOrNull { it.id == step.id }
                        ?.dependsOn == step.dependsOn
                }
                existingStep ?: step.key
                    .withMetadata(FlowStep.Id.MetadataKey, step.id.value)
                    .withMetadata(NavigationFlowReference.MetadataKey, this)
                    .withMetadata(
                        NavigationResultChannel.ResultIdKey, NavigationResultChannel.Id(
                            ownerId = "NavigationFlow",
                            resultId = step.id.value,
                        )
                    )
                    .asInstance()
                    .apply {
                        metadata.addFrom(step.metadata)
                    }
                    .copy(id = step.id.value)
            }
            .asBackstack()

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
    internal fun getSteps(): List<FlowStep<Any>> = steps

    @PublishedApi
    internal fun getResultManager(): FlowResultManager = resultManager

    public companion object {
        internal object ResultFlowKey : NavigationKey.TransientMetadataKey<NavigationFlow<*>?>(
            default = null,
            name = "dev.enro.result.flow.NavigationFlow.Companion.ResultFlowKey",
        )
        internal object ResultFlowIdKey : NavigationKey.MetadataKey<String?>(
            default = null,
            name = "dev.enro.result.flow.NavigationFlow.Companion.ResultFlowIdKey",
        )
    }
}

