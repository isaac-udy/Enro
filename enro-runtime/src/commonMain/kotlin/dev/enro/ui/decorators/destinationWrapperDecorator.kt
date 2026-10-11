package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination

/**
 * Wraps every destination a renderer displays. It sits outside the movable-content gate, so it
 * runs even for a destination whose content another scene is rendering.
 */
public fun interface NavigationDestinationWrapper {
    @Composable
    public fun Wrap(destination: NavigationDestination<NavigationKey>)
}

/** The wrapper the enclosing renderer applies; `null` renders destinations unwrapped. */
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
