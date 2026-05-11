package dev.enro.core

import androidx.compose.runtime.Composable
import dev.enro.ui.LocalNavigationContainerOrNull
import dev.enro.ui.NavigationContainerState

public val parentContainer: NavigationContainerState?
    @Composable
    get() = LocalNavigationContainerOrNull.current
