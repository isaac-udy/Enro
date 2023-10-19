@file:Suppress("PackageDirectoryMismatch")

package dev.enro.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.emptyBackstack

@Composable
@Deprecated(
    message = "Please use ComposableNavigationContainer.Render() directly, and wrap this inside of a Box() or other layout if you wish to provide modifiers",
    replaceWith = ReplaceWith(
        "Box(modifier = modifier) { container.Render() }",
        "androidx.compose.foundation.layout.Box"
    )
)
public fun EnroContainer(
    modifier: Modifier = Modifier,
    container: ComposableNavigationContainer = rememberNavigationContainer(
        initialBackstack = emptyBackstack(),
        emptyBehavior = EmptyBehavior.AllowEmpty,
    ),
) {
    Box(modifier = modifier) {
        container.Render()
    }
}

