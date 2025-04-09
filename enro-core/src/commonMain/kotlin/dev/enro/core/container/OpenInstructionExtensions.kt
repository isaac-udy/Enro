package dev.enro.core.container

import androidx.savedstate.read
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.write
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.OpenPresentInstruction
import dev.enro.core.OpenPushInstruction

private const val ORIGINAL_NAVIGATION_DIRECTION = "OpenInstructionExtensions.ORIGINAL_NAVIGATION_DIRECTION"

@Suppress("UNCHECKED_CAST")
internal fun AnyOpenInstruction.asPushInstruction(): OpenPushInstruction =
    asDirection(NavigationDirection.Push)

@Suppress("UNCHECKED_CAST")
internal fun AnyOpenInstruction.asPresentInstruction(): OpenPresentInstruction =
    asDirection(NavigationDirection.Present)

@PublishedApi
internal fun AnyOpenInstruction.originalNavigationDirection(): NavigationDirection {
    val originalDirection = extras.read { this.getSavedStateOrNull(ORIGINAL_NAVIGATION_DIRECTION) }
    if (originalDirection != null) {
        return extras.read { decodeFromSavedState(getSavedState(ORIGINAL_NAVIGATION_DIRECTION)) }
    }
    return navigationDirection
}

@Suppress("UNCHECKED_CAST")
internal fun <T : NavigationDirection> AnyOpenInstruction.asDirection(direction: T): NavigationInstruction.Open<T> {
    if (navigationDirection == direction) return this as NavigationInstruction.Open<T>
    return internal.copy(
        navigationDirection = direction,
        extras = extras.apply {
            val originalDirection = read { this.getSavedStateOrNull(ORIGINAL_NAVIGATION_DIRECTION) }
            if (originalDirection != null) return@apply
            extras.write {
                putSavedState(
                    ORIGINAL_NAVIGATION_DIRECTION,
                    encodeToSavedState(navigationDirection)
                )
            }
        }
    ) as NavigationInstruction.Open<T>
}