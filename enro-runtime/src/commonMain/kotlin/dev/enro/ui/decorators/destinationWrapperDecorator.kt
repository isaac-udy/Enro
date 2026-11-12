package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination

/**
 * Wraps every destination a renderer displays. [Wrap] is called outside
 * `movableContentDecorator`, so [Wrap] is also called in a scene that lists the destination
 * while another scene renders the destination's content.
 */
public fun interface NavigationDestinationWrapper {
    @Composable
    public fun Wrap(destination: NavigationDestination<NavigationKey>)
}

/** The wrapper that the enclosing renderer provides. `null` renders destinations unwrapped. */
public val LocalNavigationDestinationWrapper: ProvidableCompositionLocal<NavigationDestinationWrapper?> =
    staticCompositionLocalOf { null }

internal fun destinationWrapperDecorator(): NavigationDestinationDecorator<NavigationKey> =
    navigationDestinationDecorator { destination ->
        val wrapper = LocalNavigationDestinationWrapper.current
        if (wrapper == null) {
            destination.Content()
        } else {
            wrapper.Wrap(destination)
        }
    }
