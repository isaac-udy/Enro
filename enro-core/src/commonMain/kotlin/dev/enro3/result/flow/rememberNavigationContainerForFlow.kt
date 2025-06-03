package dev.enro3.result.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.enro3.NavigationKey
import dev.enro3.asInstance
import dev.enro3.interceptor.builder.navigationInterceptor
import dev.enro3.result.NavigationResult
import dev.enro3.result.NavigationResultChannel
import dev.enro3.result.getResult
import dev.enro3.ui.NavigationContainerState
import dev.enro3.ui.destinations.EmptyNavigationKey
import dev.enro3.ui.rememberNavigationContainer

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

                    flow.onStepCompleted(flowStep, result.data ?: Unit)
                    cancel()
                }
                onTransition {
                    if (transition.closed.isEmpty()) continueWithTransition()
                    val flowSteps = transition.closed.mapNotNull { it.metadata.get(FlowStep.MetadataKey) }
                    // If there's more than one flow step involved in this transition, we're opting out
                    if (flowSteps.size != 1) continueWithTransition()

                    @Suppress("UNCHECKED_CAST")
                    val step = flowSteps.single() as FlowStep<Any>

                    transition.closed.onEach {
                        val resultId = it.metadata.get(NavigationResultChannel.ResultIdKey)
                        if (resultId == null) return@onEach
                        if (resultId.resultId != step.stepId) return@onEach
                        val result = it.getResult()
                        if (result !is NavigationResult.Completed<*>) return@onEach
                        flow.onStepCompleted(step, result.data ?: Unit)
                        cancel()
                    }
                    continueWithTransition()
                }
            }
        }
    ).apply {
        DisposableEffect(this) {
            flow.container = container
            onDispose {
                flow.container = null
            }
        }
    }
}

