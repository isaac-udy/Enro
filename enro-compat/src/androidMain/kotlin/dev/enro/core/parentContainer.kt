package dev.enro.core

import androidx.compose.runtime.Composable
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.NavigationContainerState

public val parentContainer: NavigationContainerState?
    @Composable
    get() {
        val parentContainer = runCatching { LocalNavigationContainer.current }
            .getOrNull()
        return parentContainer
    }