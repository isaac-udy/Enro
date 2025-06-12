package dev.enrolegacy.animation

import dev.enro.core.controller.NavigationController

internal actual fun isAnimationsDisabledForPlatform(controller: NavigationController): Boolean {
    return false
}