package dev.enro.core.controller.usecase

import dev.enro.core.controller.NavigationController

internal actual fun isAnimationsDisabledForPlatform(controller: NavigationController): Boolean {
    return false
}