package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import dev.enro.NavigationContext

public object LocalNavigationContext {
    private val LocalNavigationContext = compositionLocalOf<NavigationContext?> { null }

    public val current: NavigationContext
        @Composable get() = LocalNavigationContext.current ?: findRootNavigationContext()

    public infix fun provides(
        navigationContext: NavigationContext
    ): ProvidedValue<NavigationContext> {
        @Suppress("UNCHECKED_CAST")
        return LocalNavigationContext.provides(navigationContext) as ProvidedValue<NavigationContext>
    }
}

@Composable
internal expect fun findRootNavigationContext(): NavigationContext.Root