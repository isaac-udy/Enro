package dev.enro.result.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.interceptor.builder.navigationInterceptor
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
                    cancelAnd {
                        flow.onStepCompleted(flowStep, data ?: Unit)
                        flow.update()
                    }
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

