package dev.enro.platform.desktop

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import java.awt.Frame

public val LocalRootFrame: ProvidableCompositionLocal<Frame> = staticCompositionLocalOf {
    error("No root window provided")
}