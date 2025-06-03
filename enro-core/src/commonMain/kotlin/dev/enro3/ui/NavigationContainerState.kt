package dev.enro3.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.enro3.NavigationBackstack
import dev.enro3.NavigationContainer
import dev.enro3.NavigationKey
import dev.enro3.NavigationOperation

public class NavigationContainerState(
    public val container: NavigationContainer,
) {
    public val key: NavigationContainer.Key = container.key

    /** Progress of the current predictive back gesture (0.0 to 1.0) */
    public var progress: Float by mutableFloatStateOf(0f)
        internal set

    /** Whether a predictive back gesture is currently in progress */
    public var inPredictiveBack: Boolean by mutableStateOf(false)
        internal set

    /** Whether the navigation state is settled (no animations running) */
    public var isSettled: Boolean by mutableStateOf(true)
        internal set

    public var destinations: List<NavigationDestination<NavigationKey>> by mutableStateOf(emptyList())
        internal set

    public val backstack: NavigationBackstack
        @Composable
        get() = container.backstack.collectAsState().value

    public fun execute(operation: NavigationOperation) {
        container.execute(operation)
    }
}