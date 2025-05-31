package dev.enro3.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import dev.enro3.NavigationBackstack
import dev.enro3.NavigationContainer
import dev.enro3.interceptor.NavigationInterceptor
import dev.enro3.interceptor.NoOpNavigationInterceptor

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    backstack: NavigationBackstack,
    // Need to get parent interceptors too from controller
    interceptor: NavigationInterceptor = NoOpNavigationInterceptor,
): NavigationContainer {
    val parent = runCatching { LocalNavigationContainer.current }

    return remember {
        NavigationContainer(
            key = key,
            backstack = backstack,
            parent = parent.getOrNull(),
            interceptor = interceptor
        )
    }
}