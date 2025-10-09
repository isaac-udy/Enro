package dev.enro.result.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import dev.enro.NavigationContainer
import dev.enro.NavigationContainerFilter
import dev.enro.NavigationKey
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.rememberNavigationContainer

@Composable
public fun rememberNavigationContainerForFlow(
    flow: NavigationFlow<*>,
): NavigationContainerState {
    return rememberNavigationContainer(
        key = NavigationContainer.Key("NavigationFlow"),
        backstack = listOf(),
        filter = NavigationContainerFilter(
            fromChildrenOnly = true,
            block = { true },
        ),
        interceptor = remember {
            navigationInterceptor {
                onClosed<NavigationKey> {
                    val stepId = instance.flowStepId
                    if (stepId != null && !isSilent) {
                        flow.onStepClosed(stepId)
                    }
                    continueWithClose()
                }
                onCompleted<NavigationKey> {
                    val stepId = instance.flowStepId
                        ?: instance.metadata.get(NavigationResultChannel.ResultIdKey)
                            ?.let { resultId ->
                                flow.getSteps()
                                    .firstOrNull { it.id.value == resultId.resultId }
                                    ?.id
                            }
                    if (stepId == null) continueWithComplete()
                    cancelAnd {
                        flow.onStepCompleted(stepId, data ?: Unit)
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

@Composable
public fun rememberNavigationContainerForFlow(
    viewModel: ViewModel,
): NavigationContainerState {
    return rememberNavigationContainerForFlow(
        flow = remember(viewModel) {
            viewModel.navigationFlow ?: error("No NavigationFlow found on ViewModel $viewModel")
        }
    )
}
