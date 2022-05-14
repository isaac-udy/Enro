package dev.enro.core.container

import dev.enro.core.*
import kotlinx.parcelize.Parcelize

internal fun AnyOpenInstruction.asContainerRoot(): OpenForwardInstruction {
    this as NavigationInstruction.Open<NavigationDirection>
    return internal.copy(
        navigationDirection = NavigationDirection.Forward
    ) as OpenForwardInstruction
}