package dev.enro.core.container

import dev.enro.core.*

@Suppress("UNCHECKED_CAST")
internal fun AnyOpenInstruction.asPushInstruction(): OpenPushInstruction =
    asDirection(NavigationDirection.Push)

@Suppress("UNCHECKED_CAST")
internal fun AnyOpenInstruction.asPresentInstruction(): OpenPresentInstruction =
    asDirection(NavigationDirection.Present)

@Suppress("UNCHECKED_CAST")
internal fun <T: NavigationDirection> AnyOpenInstruction.asDirection(direction: T): NavigationInstruction.Open<T> {
    if(navigationDirection == direction) return this as NavigationInstruction.Open<T>
    return internal.copy(
        navigationDirection = direction
    ) as NavigationInstruction.Open<T>
}