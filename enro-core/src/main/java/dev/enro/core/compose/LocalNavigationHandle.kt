package dev.enro.core.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.enro.core.NavigationHandle

public val LocalNavigationHandle: ProvidableCompositionLocal<NavigationHandle?> =
    compositionLocalOf {
        null
    }
