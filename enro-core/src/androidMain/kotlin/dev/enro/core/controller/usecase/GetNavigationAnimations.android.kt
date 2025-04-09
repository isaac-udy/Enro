package dev.enro.core.controller.usecase

import android.provider.Settings
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application

internal actual fun isAnimationsDisabledForPlatform(controller: NavigationController): Boolean {
    val animationScale = runCatching {
        Settings.Global.getFloat(
            controller.application.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE
        )
    }.getOrDefault(1.0f)

    return animationScale < 0.01f
}