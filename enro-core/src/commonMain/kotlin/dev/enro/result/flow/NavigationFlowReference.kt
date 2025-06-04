package dev.enro.result.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.navigationHandle
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
) {
    internal object MetadataKey : NavigationKey.TransientMetadataKey<NavigationFlow<*>?>(null)
}

@ExperimentalEnroApi
public fun NavigationHandle<*>.getNavigationFlow(reference: NavigationFlowReference): NavigationFlow<*> {
    val flow = instance.metadata.get(NavigationFlowReference.MetadataKey)
    requireNotNull(flow) {
        "NavigationFlow with ${reference.id} is not attached to NavigationHandle: $reference"
    }
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