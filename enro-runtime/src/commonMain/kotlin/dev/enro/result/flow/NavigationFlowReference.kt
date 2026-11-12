package dev.enro.result.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.navigationHandle
import kotlinx.serialization.Serializable

/**
 * A serializable reference to a [NavigationFlow]. [NavigationFlowScope.navigationFlowReference]
 * provides the reference while the flow is built. Pass the reference in the [NavigationKey] of
 * a step, and the destination of the step resolves the [NavigationFlow] with [getNavigationFlow].
 */
@Serializable
public class NavigationFlowReference internal constructor(
    internal val id: String,
) {
    internal object MetadataKey : NavigationKey.TransientMetadataKey<NavigationFlow<*>?>(
        default = null,
        name = "dev.enro.result.flow.NavigationFlowReference.MetadataKey",
    )
}

/**
 * Returns the [NavigationFlow] that opened this destination as a step.
 *
 * Throws [IllegalArgumentException] if this destination was not opened as a step of a flow, or
 * if [reference] refers to a different flow.
 */
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
public fun rememberNavigationFlowReference(
    reference: NavigationFlowReference,
): NavigationFlow<*> {
    val navigationHandle = navigationHandle()
    return remember(navigationHandle) {
        navigationHandle.getNavigationFlow(reference)
    }
}