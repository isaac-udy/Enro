package dev.enro3.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * The destination IDs to render in the current NavigationScene, in the sense of the target of the animation for
 * an [AnimatedContent] that is transitioning between different scenes.
 */
public val LocalDestinationsToRenderInCurrentScene: ProvidableCompositionLocal<Set<String>> =
    compositionLocalOf {
        throw IllegalStateException(
            "Unexpected access to LocalDestinationsToRenderInCurrentScene. You should only " +
                    "access LocalDestinationsToRenderInCurrentScene inside a NavigationDestination passed " +
                    "to NavigationDisplay."
        )
    }