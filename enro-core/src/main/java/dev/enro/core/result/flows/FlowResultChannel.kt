package dev.enro.core.result.flows

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.enro.core.*
import dev.enro.core.container.toBackstack
import dev.enro.core.result.NavigationResultChannel
import dev.enro.core.result.internal.ResultChannelImpl
import dev.enro.core.result.registerForNavigationResultWithKey
import dev.enro.extensions.getParcelableListCompat
import dev.enro.android.viewmodel.getNavigationHandle
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

internal fun interface CreateResultChannel {
    operator fun invoke(
        onClosed: (Any) -> Unit,
        onResult: (NavigationKey.WithResult<*>, Any) -> Unit
    ): NavigationResultChannel<Any, NavigationKey.WithResult<Any>>
}

@dev.enro.annotations.ExperimentalEnroApi
public class NavigationFlow<T> internal constructor(
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
        onClosed = { key ->
            val step = key as? FlowStep<Any> ?: return@registerForNavigationResult
            if (step.stepId == steps.lastOrNull()?.stepId) {
                resultManager.clear(step)
                steps = steps.dropLast(1)
            }
        },
        onResult = { key, result ->
            val step = key as? FlowStep<Any> ?: return@registerForNavigationResult
            resultManager.set(step, result)
            next()
        },
    )

    init {
        savedStateHandle.setSavedStateProvider(STEPS_KEY) {
            bundleOf(STEPS_KEY to ArrayList(steps))
        }
    }

    public fun next() {
        val flowScope = NavigationFlowScope(resultManager)
        runCatching { onCompleted(flowScope.flow()) }
            .recover {
                when(it) {
                    is NavigationFlowScope.NoResult -> {}
                    is NavigationFlowScope.Escape -> return
                    else -> throw it
                }
            }
            .getOrThrow()

        val resultChannelId = (resultChannel as ResultChannelImpl<*,*>).id
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

            val instructions = steps.map { step ->
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
            val finalInstructions = instructions
                .filter { it.navigationDirection == NavigationDirection.Push }
                .plus(
                    instructions.lastOrNull().takeIf { it?.navigationDirection == NavigationDirection.Present }
                )
                .filterNotNull()

            setBackstack(finalInstructions.toBackstack())
        }
    }

    internal companion object {
        const val IS_PUSHED_IN_FLOW = "NavigationFlow.IS_PUSHED_IN_FLOW"
        const val STEPS_KEY = "NavigationFlow.STEPS_KEY"
    }
}

public fun <T> ViewModel.registerForFlowResult(
    savedStateHandle: SavedStateHandle,
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    return PropertyDelegateProvider { thisRef, property ->
        val navigationHandle = getNavigationHandle()
        val resultManager = FlowResultManager.create(navigationHandle, savedStateHandle)

        val navigationFlow = NavigationFlow(
            savedStateHandle = savedStateHandle,
            navigation = navigationHandle,
            resultManager = resultManager,
            registerForNavigationResult = { onClosed, onResult ->
                registerForNavigationResultWithKey(
                    onClosed = onClosed,
                    onResult = onResult,
                ).getValue(thisRef, property)
            },
            flow = flow,
            onCompleted = onCompleted,
        )
        ReadOnlyProperty { _, _ -> navigationFlow }
    }
}