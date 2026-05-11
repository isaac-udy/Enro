package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import dev.enro.NavigationContext
import dev.enro.context.RootContext
import dev.enro.ui.LocalNavigationContext.current

public object LocalNavigationContext {
    private val LocalNavigationContext = compositionLocalOf<NavigationContext?> { null }

    public val current: NavigationContext
        @Composable get() {
            val current = LocalNavigationContext.current ?: findRootNavigationContext()
            return remember { current }
        }

    /**
     * Null-safe sibling of [current] that returns `null` when no navigation
     * context is available, instead of falling through to
     * [findRootNavigationContext] (which throws on platforms / surfaces with
     * no host activity — e.g. Paparazzi snapshot tests).
     */
    public val currentOrNull: NavigationContext?
        @Composable get() = LocalNavigationContext.current

    public infix fun provides(
        navigationContext: NavigationContext
    ): ProvidedValue<NavigationContext> {
        @Suppress("UNCHECKED_CAST")
        return LocalNavigationContext.provides(navigationContext) as ProvidedValue<NavigationContext>
    }
}

@Composable
internal expect fun findRootNavigationContext(): RootContext