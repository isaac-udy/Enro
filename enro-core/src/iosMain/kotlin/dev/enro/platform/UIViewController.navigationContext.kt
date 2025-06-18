package dev.enro.platform

import dev.enro.context.RootContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject

public val UIViewController.navigationContext: RootContext
    get() {
        return internalNavigationContext ?: error("UIViewController $this is not an EnroUIViewController, and does not have a navigation context.")
    }

@OptIn(ExperimentalForeignApi::class)
private val UIViewControllerNavigationContextKey = kotlinx.cinterop.staticCFunction<Unit> {}

@OptIn(ExperimentalForeignApi::class)
internal var UIViewController.internalNavigationContext: RootContext?
    get() {
        return objc_getAssociatedObject(
            this,
            UIViewControllerNavigationContextKey
        ) as? RootContext?
    }
    set(value) {
        objc_setAssociatedObject(
            `object` = this,
            key = UIViewControllerNavigationContextKey,
            value = value,
            policy = OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )
    }
