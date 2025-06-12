package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay

@Composable
public fun NavigationContainerState.Render() {
    NavigationDisplay(this)
}