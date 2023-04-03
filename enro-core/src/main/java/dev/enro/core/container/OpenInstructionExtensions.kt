package dev.enro.core.container

import dev.enro.core.*

private const val ORIGINAL_NAVIGATION_DIRECTION = "OpenInstructionExtensions.ORIGINAL_NAVIGATION_DIRECTION"

@Suppress("UNCHECKED_CAST")
internal fun AnyOpenInstruction.asPushInstruction(): OpenPushInstruction =
    asDirection(NavigationDirection.Push)

@Suppress("UNCHECKED_CAST")
internal fun AnyOpenInstruction.asPresentInstruction(): OpenPresentInstruction =
    asDirection(NavigationDirection.Present)

@PublishedApi
internal fun AnyOpenInstruction.originalNavigationDirection(): NavigationDirection {
    if (extras.containsKey(ORIGINAL_NAVIGATION_DIRECTION))
        return extras[ORIGINAL_NAVIGATION_DIRECTION] as NavigationDirection
    return navigationDirection
}

@Suppress("UNCHECKED_CAST")
internal fun <T: NavigationDirection> AnyOpenInstruction.asDirection(direction: T): NavigationInstruction.Open<T> {
    if(navigationDirection == direction) return this as NavigationInstruction.Open<T>
    return internal.copy(
        navigationDirection = direction,
        extras = extras.apply {
            if (containsKey(ORIGINAL_NAVIGATION_DIRECTION)) return@apply
            put(ORIGINAL_NAVIGATION_DIRECTION, navigationDirection)
        }
    ) as NavigationInstruction.Open<T>
}