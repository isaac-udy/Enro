package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.NavigationOperation

public class NavigationContainerState(
    public val container: NavigationContainer,
    public val emptyBehavior: EmptyBehavior,
) {

    public val key: NavigationContainer.Key = container.key

    /** Progress of the current predictive back gesture (0.0 to 1.0) */
    public var predictiveBackProgress: Float by mutableFloatStateOf(0f)
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

    @Deprecated("TODO BETTER DEPRECATION MESSAGE")
    @Composable
    public fun Render() {
        NavigationDisplay(this)
    }
}

