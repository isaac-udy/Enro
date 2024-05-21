package dev.enro.core.result.flows

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.usecase.extras
import dev.enro.core.onActiveContainer
import dev.enro.core.result.NavigationResultChannel
import dev.enro.core.result.NavigationResultScope
import dev.enro.core.result.internal.ResultChannelImpl
import dev.enro.core.result.registerForNavigationResult
import dev.enro.extensions.getParcelableListCompat
import dev.enro.viewmodel.getNavigationHandle
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

internal fun interface CreateResultChannel {
    operator fun invoke(
        onClosed: NavigationResultScope<*, *>.() -> Unit,
        onResult: NavigationResultScope<*, *>.(Any) -> Unit
    ): NavigationResultChannel<Any, NavigationKey.WithResult<Any>>
}

@dev.enro.annotations.ExperimentalEnroApi
public class NavigationFlow<T> internal constructor(
    internal val reference: NavigationFlowReference,
    private val savedStateHandle: SavedStateHandle,
    private val navigation: NavigationHandle,
    private val resultManager: FlowResultManager,
    private val registerForNavigationResult: CreateResultChannel,
    private val flow: NavigationFlowScope.() -> T,
    private val onCompleted: (T) -> Unit,
) {
    private var steps: List<FlowStep<out Any>> = savedStateHandle.get<Bundle>(STEPS_KEY)
        ?.getParcelableListCompat<FlowStep<out Any>>(STEPS_KEY)
        .orEmpty()

    @Suppress("UNCHECKED_CAST")
    private val resultChannel = registerForNavigationResult(
        onClosed = {
            val step = key as? FlowStep<Any> ?: return@registerForNavigationResult
            resultManager.clear(step)
            steps = steps
                .dropLastWhile { step.stepId != it.stepId }
                .dropLast(1)
                .dropLastWhile { it.isTransient }
        },
        onResult = { result ->
            val step = key as? FlowStep<Any> ?: return@registerForNavigationResult
            resultManager.set(step, result)
            update()
        },
    )

    init {
        savedStateHandle.setSavedStateProvider(STEPS_KEY) {
            bundleOf(STEPS_KEY to ArrayList(steps))
        }
    }

    /**
     * This method is used to cause the flow to re-evaluate it's current state. This happens once automatically when the flow
     * is created, and then once every time a step returns a result, or a step is edited using [FlowStepActions]. However, you
     * may want to call this method yourself if there is external state that may have changed that would affect the flow.
     *
     * An example of where this is useful is if you wanted to call a suspending function, and then update external state
     * based on the result of the suspending function before resuming the flow. An example of how this would work:
     * ```
     * class ExampleViewModel(
     *     savedStateHandle: SavedStateHandle,
     * ) : ViewModel() {
     *     private var suspendingFunctionInvoked = false
     *
     *     private val resultFlow by registerForFlowResult(
     *         savedStateHandle = savedStateHandle,
     *         flow = {
     *             // ...
     *             if (!suspendingFunctionInvoked) {
     *                 performSuspendingAction()
     *                 escape()
     *             }
     *             // ...
     *         },
     *         onCompleted = { /*...*/ }
     *     )
     *
     *     private fun performSuspendingAction() {
     *         viewModelScope.launch {
     *             delay(1000)
     *             suspendingFunctionInvoked = true
     *             resultFlow.update()
     *         }
     *     }
     * }
     * ```
     *
     * In the example above, when the if statement is reached, and "suspendingFunctionInvoked" is not true, the flow will call
     * "performSuspendingAction" and then immediately call "escape". The call to "escape" will cause the flow to stop evaluating
     * which steps should occur. The call to [update] inside "performSuspendingAction" will cause the flow to re-evaluate
     * the state, and continue the flow from where it left off. Because "suspendingFunctionInvoked" will have been set to true,
     * the flow won't execute that if statement, and will instead continue on to whatever logic is next.
     */
    public fun update() {
        val flowScope = NavigationFlowScope(this, resultManager, reference)
        runCatching { return@update onCompleted(flowScope.flow()) }
            .recover {
                when (it) {
                    is NavigationFlowScope.NoResult -> {}
                    is NavigationFlowScope.Escape -> return
                    else -> throw it
                }
            }
            .getOrThrow()

        val resultChannelId = (resultChannel as ResultChannelImpl<*, *>).id
        val oldSteps = steps
        steps = flowScope.steps
        navigation.onActiveContainer {
            val existingInstructions = backstack
                .mapNotNull { instruction ->
                    val flowKey = instruction.internal.resultKey as? FlowStep<Any> ?: return@mapNotNull null
                    val step = steps.firstOrNull { it.stepId == flowKey.stepId } ?: return@mapNotNull null
                    step to instruction
                }
                .groupBy { it.first.stepId }
                .mapValues { it.value.lastOrNull() }

            val instructions = steps
                .filterIndexed { index, flowStep ->
                    if (index == steps.lastIndex) return@filterIndexed true
                    !flowStep.isTransient
                }
                .map { step ->
                    val existingStep = existingInstructions[step.stepId]?.second?.takeIf {
                        oldSteps
                            .firstOrNull { it.stepId == step.stepId }
                            ?.dependsOn == step.dependsOn
                    }
                    existingStep ?: NavigationInstruction.Open.OpenInternal(
                        navigationDirection = step.direction,
                        navigationKey = step.key,
                        resultKey = step,
                        resultId = resultChannelId,
                        extras = mutableMapOf(
                            IS_PUSHED_IN_FLOW to (step.direction is NavigationDirection.Push)
                        )
                    )
                }
            setBackstack(instructions.toBackstack())
        }
    }

    @PublishedApi
    internal fun getSteps(): List<FlowStep<out Any>> = steps

    @PublishedApi
    internal fun getResultManager(): FlowResultManager = resultManager

    internal companion object {
        const val IS_PUSHED_IN_FLOW = "NavigationFlow.IS_PUSHED_IN_FLOW"
        const val STEPS_KEY = "NavigationFlow.STEPS_KEY"
        const val RESULT_FLOW_ID = "NavigationFlow.RESULT_FLOW_ID"
        const val RESULT_FLOW = "NavigationFlow.RESULT_FLOW"
    }
}

/**
 * This method creates a NavigationFlow in the scope of a ViewModel. There can only be one NavigationFlow created within each
 * NavigationDestination. The [flow] lambda will be invoked multiple times over the lifecycle of the NavigationFlow, and should
 * generally not cause external side effects. The [onCompleted] lambda will be invoked when the flow completes and returns a
 * result.
 *
 * [NavigationFlow.update] is triggered automatically as part of this function, you do not need to manually call update to
 * begin the flow.
 */
public fun <T> ViewModel.registerForFlowResult(
    savedStateHandle: SavedStateHandle,
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    return PropertyDelegateProvider { thisRef, property ->
        val navigationHandle = getNavigationHandle()

        val resultFlowId = property.name
        val boundResultFlowId = navigationHandle.extras[NavigationFlow.RESULT_FLOW_ID]
        require(boundResultFlowId == null || boundResultFlowId == resultFlowId) {
            "Only one registerForFlowResult can be created per NavigationHandle. Found an existing result flow for $boundResultFlowId."
        }
        navigationHandle.extras[NavigationFlow.RESULT_FLOW_ID] = resultFlowId

        val resultManager = FlowResultManager.create(navigationHandle, savedStateHandle)
        val navigationFlow = NavigationFlow(
            reference = NavigationFlowReference(resultFlowId),
            savedStateHandle = savedStateHandle,
            navigation = navigationHandle,
            resultManager = resultManager,
            registerForNavigationResult = { onClosed, onResult ->
                registerForNavigationResult(
                    onClosed = onClosed,
                    onResult = onResult,
                ).getValue(thisRef, property)
            },
            flow = flow,
            onCompleted = onCompleted,
        )
        navigationHandle.extras[NavigationFlow.RESULT_FLOW] = navigationFlow
        navigationFlow.update()
        ReadOnlyProperty { _, _ -> navigationFlow }
    }
}