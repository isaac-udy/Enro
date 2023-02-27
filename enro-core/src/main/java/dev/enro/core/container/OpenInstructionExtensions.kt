package dev.enro.core.container

import dev.enro.core.*
import dev.enro.extensions.getParcelableCompat

private const val ORIGINAL_NAVIGATION_DIRECTION = "OpenInstructionExtensions.ORIGINAL_NAVIGATION_DIRECTION"

@Suppress("UNCHECKED_CAST")
internal fun AnyOpenInstruction.asPushInstruction(): OpenPushInstruction =
    asDirection(NavigationDirection.Push)

@Suppress("UNCHECKED_CAST")
internal fun AnyOpenInstruction.asPresentInstruction(): OpenPresentInstruction =
    asDirection(NavigationDirection.Present)

internal fun AnyOpenInstruction.originalNavigationDirection(): NavigationDirection {
    if (additionalData.containsKey(ORIGINAL_NAVIGATION_DIRECTION))
        return additionalData.getParcelableCompat(ORIGINAL_NAVIGATION_DIRECTION)!!
    return navigationDirection
}

@Suppress("UNCHECKED_CAST")
internal fun <T: NavigationDirection> AnyOpenInstruction.asDirection(direction: T): NavigationInstruction.Open<T> {
    if(navigationDirection == direction) return this as NavigationInstruction.Open<T>
    return internal.copy(
        navigationDirection = direction,
        additionalData = additionalData.apply {
            if (containsKey(ORIGINAL_NAVIGATION_DIRECTION)) return@apply
            putParcelable(ORIGINAL_NAVIGATION_DIRECTION, navigationDirection)
        }
    ) as NavigationInstruction.Open<T>
}