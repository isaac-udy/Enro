package dev.enro.core.container

import dev.enro.core.*

internal fun AnyOpenInstruction.asPushInstruction(): OpenPushInstruction {
    if(navigationDirection is NavigationDirection.Push) return this as OpenPushInstruction
    return internal.copy(
        navigationDirection = NavigationDirection.Push
    ) as OpenPushInstruction
}

internal fun AnyOpenInstruction.asPresentInstruction(): OpenPresentInstruction {
    if(navigationDirection is NavigationDirection.Present) return this as OpenPresentInstruction
    return internal.copy(
        navigationDirection = NavigationDirection.Present
    ) as OpenPresentInstruction
}

internal fun <T: NavigationDirection> AnyOpenInstruction.asDirection(direction: T): NavigationInstruction.Open<T> {
    if(navigationDirection == direction) return this as NavigationInstruction.Open<T>
    return internal.copy(
        navigationDirection = direction
    ) as NavigationInstruction.Open<T>
}