package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.enro.context.RootContext

public val LocalRootContext: ProvidableCompositionLocal<RootContext> = staticCompositionLocalOf {
    error("No RootContext provided")
}

@Composable
internal actual fun findRootNavigationContext(): RootContext {
    return LocalRootContext.current
}