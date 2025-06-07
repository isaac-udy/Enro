package dev.enro.result.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import dev.enro.result.clearResult
import dev.enro.result.getResult
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.destinations.EmptyNavigationKey
import dev.enro.ui.rememberNavigationContainer

@Composable
public fun rememberNavigationContainerForFlow(
    flow: NavigationFlow<*>,
): NavigationContainerState {
    return rememberNavigationContainer(
        backstack = listOf(EmptyNavigationKey.asInstance()),
        interceptor = remember {
            navigationInterceptor {
                onClosed<NavigationKey> {
                    val flowStep = instance.metadata.get(FlowStep.MetadataKey) as? FlowStep<Any>
                    if (flowStep != null) {
                        flow.onStepClosed(instance)
                    }
                    continueWithClose()
                }
                onCompleted<NavigationKey> {
                    val flowStep = instance.metadata.get(FlowStep.MetadataKey) as? FlowStep<Any>
                    if (flowStep == null) continueWithComplete()

                    val result = instance.getResult() as? NavigationResult.Completed
                    if (result == null) continueWithComplete()

                    cancelAnd {
                        instance.clearResult()
                        flow.onStepCompleted(flowStep, result.data ?: Unit)
                        flow.update()
                    }
                }
                onTransition {
                    if (transition.closed.isEmpty()) continueWithTransition()
                    val flowSteps = transition.closed.mapNotNull { it.metadata.get(FlowStep.MetadataKey) }
                    // If there's more than one flow step involved in this transition, we're opting out
                    if (flowSteps.size != 1) continueWithTransition()

                    @Suppress("UNCHECKED_CAST")
                    val step = flowSteps.single() as FlowStep<Any>

                    val completed = transition.closed.mapNotNull {
                        val resultId = it.metadata.get(NavigationResultChannel.ResultIdKey)
                        if (resultId == null) return@mapNotNull null
                        if (resultId.resultId != step.stepId) return@mapNotNull null
                        val result = it.getResult()
                        if (result !is NavigationResult.Completed<*>) return@mapNotNull null

                        it.clearResult()
                        flow.onStepCompleted(step, result.data ?: Unit)
                    }
                    if (completed.isNotEmpty()) {
                        cancelAnd { flow.update() }
                    }
                    continueWithTransition()
                }
            }
        }
    ).apply {
        val state = this
        DisposableEffect(this) {
            flow.container = state
            onDispose {
                flow.container = null
            }
        }
    }
}

