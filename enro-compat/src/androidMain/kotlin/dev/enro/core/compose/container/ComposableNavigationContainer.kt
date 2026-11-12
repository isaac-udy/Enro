package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay

/**
 * Renders the container with [NavigationDisplay] and the default parameters.
 *
 * @deprecated Call [NavigationDisplay], which takes a modifier and animations. The modifier
 * parameter replaces a `Box(modifier = ...)` around `container.Render()`.
 */
@Deprecated(
    message = "Use NavigationDisplay instead for more configuration options",
    replaceWith = ReplaceWith(
        expression = "NavigationDisplay(this)",
        imports = ["dev.enro.ui.NavigationDisplay"]
    )
)
@Composable
public fun NavigationContainerState.Render() {
    NavigationDisplay(this)
}