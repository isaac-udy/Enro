package dev.enro.animation

import dev.enro.core.controller.NavigationController

internal expect fun isAnimationsDisabledForPlatform(controller: NavigationController): Boolean
