package dev.enro3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A NavigationContainer is an identifiable backstack (using navigation container key), which
 * provides the rendering context for a backstack.
 *
 * It's probably the NavigationContainer that needs to be able to host NavigationScenes/NavigationRenderers\
 *
 * Instead of having a CloseParent/AllowEmpty, we should provide a special "Empty" instruction here (maybe even with a
 * placeholder) so that the close behaviour is always consistent (easier for predictive back stuff).
 */
public class NavigationContainer internal constructor(
    public val key: Key,
    backstack: NavigationBackstack = emptyList(),
    public val parent: NavigationContainer? = null,
) {
    private val mutableBackstack: MutableStateFlow<NavigationBackstack> = MutableStateFlow(backstack)
    public val backstack: StateFlow<NavigationBackstack> = mutableBackstack

    public val interceptor: NavigationInterceptor = NavigationResultChannel.Interceptor
//    public val filter: NavigationInstructionFilter = TODO()

    public fun execute(operation: NavigationOperation) {
        val transition = interceptor.intercept(
            transition = NavigationTransition(
                from = backstack.value,
                to = operation(backstack.value),
            )
        )
        if (transition == null) return
        mutableBackstack.value = transition.to
    }

    public data class Key(val name: String)
}

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    backstack: NavigationBackstack,
): NavigationContainer {
    val parent = runCatching { LocalNavigationContainer.current }

    return remember {
        NavigationContainer(
            key = key,
            backstack = backstack,
            parent = parent.getOrNull(),
        )
    }
}