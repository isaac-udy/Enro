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
     * Returns `null` when no navigation context is provided. [current] looks up the root
     * navigation context instead, and that lookup throws when the composition has no host with
     * a root context, for example in a Paparazzi snapshot test.
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