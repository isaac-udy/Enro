package dev.enro.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.navigationHandle

public open class NavigationDestinationProvider<T : NavigationKey>(
    public val metadata: Map<String, Any> = emptyMap(),
    private val content: @Composable NavigationDestinationScope<T>.() -> Unit,
) {
    public fun create(instance: NavigationKey.Instance<T>): NavigationDestination<T> {
        return NavigationDestination.create(
            instance = instance,
            metadata = metadata,
            content = content,
        )
    }
}

public class NavigationDestination<T : NavigationKey> private constructor(
    public val instance: NavigationKey.Instance<T>,
    public val metadata: Map<String, Any> = emptyMap(),
    public val content: @Composable () -> Unit,
) {
    public companion object {
        @OptIn(ExperimentalSharedTransitionApi::class)
        public fun <T: NavigationKey> create(
            instance: NavigationKey.Instance<T>,
            metadata: Map<String, Any> = emptyMap(),
            content: @Composable NavigationDestinationScope<T>.() -> Unit,
        ): NavigationDestination<T> {
            return NavigationDestination(
                instance = instance,
                metadata = metadata,
                content = {
                    @Suppress("UNCHECKED_CAST")
                    val navigation = navigationHandle<NavigationKey>() as NavigationHandle<T>
                    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current
                    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
                    val scope = remember(animatedVisibilityScope, sharedTransitionScope) {
                        NavigationDestinationScope(
                            navigation = navigation,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                    content.invoke(scope)
                },
            )
        }

        // createWithoutScope is used to create a NavigationDestination that does not include a NavigationDestinationScope
        // as part of the content lambda; this is important for creating decorators, as some elements required to create
        // the NavigationScope need to be provided by some of the internal decorators (e.g. lifecycle, context, etc)
        internal fun <T: NavigationKey> createWithoutScope(
            instance: NavigationKey.Instance<T>,
            metadata: Map<String, Any> = emptyMap(),
            content: @Composable () -> Unit,
        ): NavigationDestination<T> {
            return NavigationDestination(
                instance = instance,
                metadata = metadata,
                content = {
                    content.invoke()
                },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
public class NavigationDestinationScope<T : NavigationKey>(
    public val navigation: NavigationHandle<T>,
    private val animatedVisibilityScope: AnimatedVisibilityScope,
    private val sharedTransitionScope: SharedTransitionScope,
) : SharedTransitionScope by sharedTransitionScope, AnimatedVisibilityScope by animatedVisibilityScope

// We probably want to get rid of push/present and let scenes handle those

public fun <T: NavigationKey> navigationDestination(
    metadata: Map<String, Any> = emptyMap(),
    content: @Composable NavigationDestinationScope<T>.() -> Unit
): NavigationDestinationProvider<T> {
    return NavigationDestinationProvider(metadata, content)
}
