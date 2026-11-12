package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.enro.NavigationKey
import dev.enro.ui.LocalEntriesToExcludeFromCurrentScene

/**
 * Returns a decorator that wraps each destination in a [movableContentOf], so that the
 * destination keeps its state when a renderer moves the destination to another place in the
 * composition, for example to another scene.
 *
 * The decorator renders no content for a destination whose id is in
 * [LocalEntriesToExcludeFromCurrentScene].
 *
 * Place this decorator before every decorator that remembers state for the destination. State
 * remembered outside the [movableContentOf] does not move with the destination.
 */
@Composable
public fun rememberMovableContentDecorator(): NavigationDestinationDecorator<NavigationKey> =
    remember {
        movableContentDecorator()
    }

internal fun movableContentDecorator(): NavigationDestinationDecorator<NavigationKey> {
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
                                "Should not be called, this should always be updated in " +
                                        "DecorateDestination with the real content"
                            )
                        }
                    )
                }
            }
        }

        // Each holder below is remembered where the holder is used, so that a destination that
        // is removed from the backstack keeps rendering and updating while animating out.
        movableContentHolderMap.getOrPut(key) {
            key(key) {
                remember {
                    movableContentOf {
                        remember { movableContentContentHolderMap.getValue(key) }.value()
                    }
                }
            }
        }

        // Another scene renders a destination that is in LocalEntriesToExcludeFromCurrentScene.
        if (!LocalEntriesToExcludeFromCurrentScene.current.contains(destination.instance.id)) {
            key(key) {
                val movableContentContentHolder = remember {
                    movableContentContentHolderMap.getValue(key)
                }
                movableContentContentHolder.value = {
                    key(destination.instance.id) {
                        destination.Content()
                    }
                }
                val movableContentHolder = remember { movableContentHolderMap.getValue(key) }
                movableContentHolder()
            }
        }
    }
}