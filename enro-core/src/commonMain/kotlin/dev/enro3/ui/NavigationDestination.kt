package dev.enro3.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro3.NavigationKey

public open class NavigationDestinationProvider<T : NavigationKey>(
    public val metadata: Map<String, Any> = emptyMap(),
    private val content: @Composable NavigationDestinationScope<T>.() -> Unit,
) {
    public fun create(instance: NavigationKey.Instance<T>): NavigationDestination<T> {
        return object : NavigationDestination<T>(
            instance = instance,
            metadata = metadata,
            content = content,
        ) {}
    }
}

public open class NavigationDestination<T : NavigationKey>(
    public val instance: NavigationKey.Instance<T>,
    public val metadata: Map<String, Any> = emptyMap(),
    content: @Composable NavigationDestinationScope<T>.() -> Unit,
) {
    private val internalContent = content

    @Composable
    @OptIn(ExperimentalSharedTransitionApi::class)
    public fun Content() {
        @Suppress("UNCHECKED_CAST")
//        val navigation = navigationHandle<NavigationKey>() as NavigationHandle<T>
        val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current
        val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
        val scope = remember(animatedVisibilityScope, sharedTransitionScope) {
            NavigationDestinationScope<T>(
//                navigation = navigation,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope
            )
        }
        internalContent.invoke(scope)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
public class NavigationDestinationScope<T : NavigationKey>(
//    public val navigation: NavigationHandle<T>,
    private val animatedVisibilityScope: AnimatedVisibilityScope,
    private val sharedTransitionScope: SharedTransitionScope,
) : SharedTransitionScope by sharedTransitionScope, AnimatedVisibilityScope by animatedVisibilityScope

public class NavigationDestinationWrapper<T : NavigationKey>(
    public val destination: NavigationDestination<T>,
    wrapper: @Composable (entry: NavigationDestination<T>) -> Unit,
) : NavigationDestination<T>(
    instance = destination.instance,
    metadata = destination.metadata,
    content = { wrapper(destination) }
)

// We probably want to get rid of push/present and let scenes handle those

public fun <T: NavigationKey> navigationDestination(
    metadata: Map<String, Any> = emptyMap(),
    content: @Composable NavigationDestinationScope<T>.() -> Unit
): NavigationDestinationProvider<T> {
    return NavigationDestinationProvider(metadata, content)
}
