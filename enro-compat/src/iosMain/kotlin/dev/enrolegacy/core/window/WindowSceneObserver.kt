package dev.enrolegacy.core.window

import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UISceneDidActivateNotification
import platform.UIKit.UISceneDidDisconnectNotification
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

@OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
)
internal class WindowSceneObserver(
    private val onSceneActivated: (UIWindowScene) -> Unit,
    private val onSceneDeactivated: (UIWindowScene) -> Unit,
) : NSObject() {
    private var isAttached = false

    fun attach() {
        isAttached = true

        NSNotificationCenter.defaultCenter.addObserver(
            observer = this,
            selector = NSSelectorFromString("sceneDidActivate:"),
            name = UISceneDidActivateNotification,
            `object` = null
        )

        NSNotificationCenter.defaultCenter.addObserver(
            observer = this,
            selector = NSSelectorFromString("sceneDidDeactivate:"),
            name = UISceneDidDisconnectNotification,
            `object` = null
        )
    }

    @Throws(Throwable::class)
    fun detatch() {
        isAttached = false
        NSNotificationCenter.defaultCenter.removeObserver(this)
    }

    @ObjCAction
    fun sceneDidActivate(notification: NSNotification) {
        val scene = notification.`object` as? UIWindowScene
        if (scene != null) {
            onSceneActivated.invoke(scene)
        }
    }

    @ObjCAction
    fun sceneDidDeactivate(notification: NSNotification) {
        val scene = notification.`object` as? UIWindowScene
        if (scene != null) {
            onSceneDeactivated.invoke(scene)
        }
    }
}