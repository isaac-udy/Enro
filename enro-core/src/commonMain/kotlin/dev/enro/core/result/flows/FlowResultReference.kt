package dev.enro.core.result.flows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.core.NavigationHandle
import dev.enro.core.compose.navigationHandle
import dev.enro.core.controller.usecase.extras
import dev.enro.core.getParentNavigationHandle
import kotlinx.serialization.Serializable

/**
 * NavigationFlowReference is a reference to a NavigationFlow, and is available in NavigationFlowScope when building a
 * NavigationFlow. It can be passed to a NavigationKey to allow the screen that the NavigationKey represents to interact
 * with the navigation flow and perform actions such as returning to previous steps within the flow to edit items.
 */
@Serializable
@ExperimentalEnroApi
public class NavigationFlowReference internal constructor(
    internal val id: String,
)

@ExperimentalEnroApi
public fun NavigationHandle.getNavigationFlow(reference: NavigationFlowReference): NavigationFlow<*> {
    val parent = getParentNavigationHandle() ?: error("No parent navigation handle found")
    val flow = parent.extras[NavigationFlow.RESULT_FLOW] as NavigationFlow<*>
    require(flow.reference.id == reference.id) {
        "NavigationFlowReference does not match the current flow"
    }
    return flow
}

@Composable
@ExperimentalEnroApi
public fun rememberNavigationFlowReference(
    reference: NavigationFlowReference,
): NavigationFlow<*> {
    val navigationHandle = navigationHandle()
    return remember(navigationHandle) {
        navigationHandle.getNavigationFlow(reference)
    }
}
