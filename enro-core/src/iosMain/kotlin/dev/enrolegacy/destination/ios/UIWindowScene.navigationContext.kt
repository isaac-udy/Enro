package dev.enrolegacy.destination.ios

import dev.enro.core.NavigationInstruction
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIWindowScene
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject

@OptIn(ExperimentalForeignApi::class)
private val UIWindowSceneNavigationContextKey = kotlinx.cinterop.staticCFunction<Unit> {}

@OptIn(ExperimentalForeignApi::class)
public var UIWindowScene.navigationContext: NavigationInstruction.Open<*>?
    get() {
        return objc_getAssociatedObject(
            this,
            UIWindowSceneNavigationContextKey
        ) as? NavigationInstruction.Open<*>
    }
    internal set(value) {
        objc_setAssociatedObject(
            `object` = this,
            key = UIWindowSceneNavigationContextKey,
            value = value,
            policy = OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )
    }
