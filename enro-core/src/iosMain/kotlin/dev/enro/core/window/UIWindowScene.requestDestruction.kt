package dev.enro.core.window

import dev.enro.core.internal.EnroLog
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import platform.UIKit.userActivity

internal fun UIWindowScene.requestDestruction() {
    if (!UIApplication.sharedApplication.supportsMultipleScenes) {
        EnroLog.debug("UIApplication does not support multiple scenes, so cannot destroy scene")
        return
    }
    userActivity = null
    UIApplication.sharedApplication.requestSceneSessionDestruction(
        session,
        null
    ) { error ->
        if (error != null) {
            EnroLog.error("UIWindowScene destruction failed: $error")
        }
    }
}