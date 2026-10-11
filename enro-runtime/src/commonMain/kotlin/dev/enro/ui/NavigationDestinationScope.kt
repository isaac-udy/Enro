package dev.enro.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationHandle
import dev.enro.NavigationKey

@OptIn(ExperimentalSharedTransitionApi::class)
public class NavigationDestinationScope<T : NavigationKey>(
    override val destinationMetadata: Map<String, Any>,
    override val navigation: NavigationHandle<T>,
    private val animatedVisibilityScope: AnimatedVisibilityScope,
    private val sharedTransitionScope: SharedTransitionScope,
) : NavigationDestinationContentScope<T>,
    SharedTransitionScope by sharedTransitionScope,
    AnimatedVisibilityScope by animatedVisibilityScope

/**
 * Declares a Compose UI destination, whose content can use the animation and shared-element
 * scopes of the enclosing [NavigationDisplay]. Use [navigationDestinationContent] for content
 * that works with any renderer.
 */
public fun <T: NavigationKey> navigationDestination(
    metadata: NavigationDestination.MetadataBuilder<T>.() -> Unit = { },
    content: @Composable NavigationDestinationScope<T>.() -> Unit,
): NavigationDestinationProvider<T> {
    return NavigationDestinationProvider(metadata) {
        val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current
        val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
        val scope = remember(navigation, animatedVisibilityScope, sharedTransitionScope) {
            NavigationDestinationScope(
                destinationMetadata = destinationMetadata,
                navigation = navigation,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
            )
        }
        content(scope)
    }
}
