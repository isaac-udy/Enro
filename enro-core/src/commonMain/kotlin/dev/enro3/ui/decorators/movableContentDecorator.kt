package dev.enro3.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.enro3.NavigationKey
import dev.enro3.ui.LocalDestinationsToRenderInCurrentScene

/**
 * Returns a [NavigationDestinationDecorator] that wraps each destination in a [movableContentOf]
 * to allow navigation displays to arbitrarily place destinations in different places in the
 * composable call hierarchy.
 *
 * This ensures that the same destination content is not composed multiple times in different
 * places of the hierarchy, and that the destination's state is preserved when it moves between
 * different parts of the UI.
 *
 * **Important:** This should typically be the first decorator applied to ensure that other
 * stateful decorators are moved properly inside the [movableContentOf].
 */
@Composable
public fun rememberMovableContentDecorator(): NavigationDestinationDecorator<NavigationKey> =
    remember {
        movableContentDecorator()
    }

/**
 * Creates a [NavigationDestinationDecorator] that wraps destinations in [movableContentOf].
 *
 * This decorator maintains two maps:
 * - A map of content holders that store the actual destination content
 * - A map of movable content wrappers that allow the content to be moved
 *
 * The decorator only renders destinations that are marked as visible in the current scene
 * via [LocalDestinationsToRenderInCurrentScene].
 */
internal fun movableContentDecorator(): NavigationDestinationDecorator<NavigationKey> {
    val movableContentContentHolderMap: MutableMap<String, MutableState<@Composable () -> Unit>> = mutableMapOf()
    val movableContentHolderMap: MutableMap<String, @Composable () -> Unit> = mutableMapOf()

    return navigationDestinationDecorator { destination ->
        val key = destination.instance.id

        // Get or create the content holder for this destination
        movableContentContentHolderMap.getOrPut(key) {
            key(key) {
                remember {
                    mutableStateOf(
                        @Composable {
                            error(
                                "Should not be called, this should always be updated in " +
                                        "DecorateDestination with the real content"
                            )
                        }
                    )
                }
            }
        }

        // Get or create the movable content wrapper for this destination
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

        // Only render if this destination should be visible in the current scene
        if (LocalDestinationsToRenderInCurrentScene.current.contains(destination.instance.id)) {
            key(key) {
                // In case the key is removed from the backstack while this is still
                // being rendered, we remember the MutableState directly to allow
                // updating it while we are animating out.
                val movableContentContentHolder = remember {
                    movableContentContentHolderMap.getValue(key)
                }
                // Update the state holder with the actual destination content
                movableContentContentHolder.value = { destination.content() }
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