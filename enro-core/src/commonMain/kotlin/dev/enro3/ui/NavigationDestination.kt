package dev.enro3.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.enro3.NavigationKey
import kotlin.jvm.JvmSuppressWildcards

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

public open class NavigationDestinationDecorator<T : NavigationKey>(
    internal val onPop: (key: NavigationKey.Instance<T>) -> Unit,
    internal val decorator: @Composable (entry: NavigationDestination<T>) -> Unit,
)

public fun <T : NavigationKey> navigationDestinationDecorator(
    onPop: (key: NavigationKey.Instance<T>) -> Unit = {},
    decorator: @Composable (entry: NavigationDestination<T>) -> Unit,
): NavigationDestinationDecorator<T> = NavigationDestinationDecorator(onPop, decorator)

@Composable
public fun <T : NavigationKey> rememberMovableContentDestinationDecorator(): NavigationDestinationDecorator<T> =
    remember {
        movableContentDestinationDecorator()
    }

/**
 * A [NavigationDestinationDecorator] that wraps each destination in a [movableContentOf] to allow navigation displays to
 * arbitrarily place destinations in different places in the composable call hierarchy and ensures that
 * the same destination content is not composed multiple times in different places of the hierarchy.
 *
 * This should likely be the first [NavigationDestinationDecorator] to ensure that other [NavigationDestinationDecorator]
 * calls that are stateful are moved properly inside the [movableContentOf].
 */
public fun <T : NavigationKey> movableContentDestinationDecorator(): NavigationDestinationDecorator<T> {
    val movableContentContentHolderMap: MutableMap<String, MutableState<@Composable () -> Unit>> = mutableMapOf()
    val movableContentHolderMap: MutableMap<String, @Composable () -> Unit> = mutableMapOf()

    return navigationDestinationDecorator { destination ->
        val key = destination.instance.id
        movableContentContentHolderMap.getOrPut(key) {
            key(key) {
                remember {
                    mutableStateOf(
                        @Composable {
                            error(
                                "Should not be called, this should always be updated in" +
                                        "DecorateDestination with the real content"
                            )
                        }
                    )
                }
            }
        }
        movableContentHolderMap.getOrPut(key) {
            key(key) {
                remember {
                    movableContentOf {
                        // In case the key is removed from the backstack while this is still
                        // being rendered, we remember the MutableState directly to allow
                        // rendering it while we are animating out.
                        remember { movableContentContentHolderMap.getValue(key) }.value()
                    }
                }
            }
        }

        if (LocalDestinationsToRenderInCurrentScene.current.contains(destination.instance.id)) {
            key(key) {
                // In case the key is removed from the backstack while this is still
                // being rendered, we remember the MutableState directly to allow
                // updating it while we are animating out.
                val movableContentContentHolder = remember {
                    movableContentContentHolderMap.getValue(key)
                }
                // Update the state holder with the actual destination content
                movableContentContentHolder.value = { destination.Content() }
                // In case the key is removed from the backstack while this is still
                // being rendered, we remember the movableContent directly to allow
                // rendering it while we are animating out.
                val movableContentHolder = remember { movableContentHolderMap.getValue(key) }
                // Finally, render the destination content via the movableContentOf
                movableContentHolder()
            }
        }
    }
}

public fun <T : NavigationKey> decorateNavigationDestination(
    destination: NavigationDestination<T>,
    destinationDecorators: List<@JvmSuppressWildcards NavigationDestinationDecorator<*>>,
): NavigationDestination<T> {
    @Suppress("UNCHECKED_CAST")
    return (destinationDecorators as List<@JvmSuppressWildcards NavigationDestinationDecorator<T>>)
        .distinct()
        .foldRight(initial = destination) { decorator, destination ->
            NavigationDestinationWrapper(
                destination = destination,
                wrapper = { wrappedDestination ->
                    decorator.decorator(wrappedDestination)
                }
            )
        }
}

// We probably want to get rid of push/present and let scenes handle those

public fun <T: NavigationKey> navigationDestination(
    metadata: Map<String, Any> = emptyMap(),
    content: @Composable NavigationDestinationScope<T>.() -> Unit
): NavigationDestinationProvider<T> {
    return NavigationDestinationProvider(metadata, content)
}
