package dev.enro.result.flow

import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.asInstance
import dev.enro.result.NavigationResultChannel
import dev.enro.withMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@ExperimentalEnroApi
public class NavigationFlow<T> internal constructor(
    internal val reference: NavigationFlowReference,
    private val resultManager: FlowResultManager,
    private val coroutineScope: CoroutineScope,
    internal var flow: NavigationFlowScope.() -> T,
    internal var onCompleted: (T) -> Unit,
) {
    private var steps: List<FlowStep<out Any>> = emptyList()
    public var container: NavigationContainer? = null
        set(value) {
            field = value
            update()
        }

    internal fun onStepCompleted(step: FlowStep<Any>, result: Any) {
        coroutineScope.launch {
            resultManager.set(step, result)
            yield()
            update()
        }
    }

    internal fun onStepClosed(instance: NavigationKey.Instance<NavigationKey>) {
        resultManager.clear(
            instance.metadata.get(FlowStep.MetadataKey) as? FlowStep<Any> ?: return
        )
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
        runCatching { return@update onCompleted(flowScope.flow()) }
            .recover {
                when (it) {
                    is NavigationFlowScope.NoResult -> {}
                    is NavigationFlowScope.Escape -> return
                    else -> throw it
                }
            }
            .getOrThrow()

        steps = flowScope.steps

        val container = container ?: return
        container.execute(
            NavigationOperation { backstack ->
                val existingSteps = backstack.associateBy { it.id }
                steps
                    .map { step ->
                        existingSteps[step.stepId] ?:
                            step.key
                                .withMetadata(FlowStep.MetadataKey, step)
                                .withMetadata(NavigationFlowReference.MetadataKey, this)
                                .withMetadata(
                                    NavigationResultChannel.ResultIdKey, NavigationResultChannel.Id(
                                        ownerId = "NavigationFlow",
                                        resultId = step.stepId,
                                    )
                                )
                                .asInstance()
                                .copy(id = step.stepId)
                    }
            }
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

