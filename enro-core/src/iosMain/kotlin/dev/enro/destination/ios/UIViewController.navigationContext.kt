package dev.enro.destination.ios

import dev.enro.core.NavigationInstruction
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject

@OptIn(ExperimentalForeignApi::class)
private val UIViewControllerNavigationContextKey = kotlinx.cinterop.staticCFunction<Unit> {}

@OptIn(ExperimentalForeignApi::class)
public var UIViewController.navigationContext: NavigationInstruction.Open<*>?
    get() {
        return objc_getAssociatedObject(
            this,
            UIViewControllerNavigationContextKey
        ) as? NavigationInstruction.Open<*>
    }
    internal set(value) {
        objc_setAssociatedObject(
            `object` = this,
            key = UIViewControllerNavigationContextKey,
            value = value,
            policy = OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )
    }
