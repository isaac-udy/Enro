package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay

/**
 * Renders the navigation container.
 * 
 * @deprecated Use NavigationDisplay instead, which allows configuration of additional options
 * such as animations and modifiers. The modifier parameter in NavigationDisplay specifically
 * removes the need for the common pattern of wrapping container.Render() invocations in a
 * Box(modifier = Modifier).
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