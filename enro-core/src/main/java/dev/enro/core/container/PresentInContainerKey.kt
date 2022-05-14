package dev.enro.core.container

import dev.enro.core.*

internal fun AnyOpenInstruction.asPushInstruction(): OpenPushInstruction {
    if(navigationDirection is NavigationDirection.Push) return this as OpenPushInstruction
    return internal.copy(
        navigationDirection = NavigationDirection.Push
    ) as OpenPushInstruction
}

internal fun AnyOpenInstruction.asPresentInstruction(): OpenPresentInstruction {
    if(navigationDirection is NavigationDirection.Push) return this as OpenPresentInstruction
    return internal.copy(
        navigationDirection = NavigationDirection.Present
    ) as OpenPresentInstruction
}