package dev.enro.core.container

import dev.enro.core.*

internal fun AnyOpenInstruction.asContainerRoot(): OpenForwardInstruction {
    this as NavigationInstruction.Open<NavigationDirection>
    return internal.copy(
        navigationDirection = NavigationDirection.Push
    ) as OpenForwardInstruction
}