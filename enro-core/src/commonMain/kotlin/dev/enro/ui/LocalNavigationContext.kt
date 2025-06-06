package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import dev.enro.NavigationContext
import dev.enro.context.RootContext

public object LocalNavigationContext {
    private val LocalNavigationContext = compositionLocalOf<NavigationContext?> { null }

    public val current: NavigationContext
        @Composable get() {
            val current = LocalNavigationContext.current ?: findRootNavigationContext()
            return remember { current }
        }

    public infix fun provides(
        navigationContext: NavigationContext
    ): ProvidedValue<NavigationContext> {
        @Suppress("UNCHECKED_CAST")
        return LocalNavigationContext.provides(navigationContext) as ProvidedValue<NavigationContext>
    }
}

@Composable
internal expect fun findRootNavigationContext(): RootContext