package dev.enro.core.controller

import android.app.Application
import androidx.annotation.Keep

/**
 * Create a NavigationController from the NavigationControllerDefinition/DSL, and immediately attach it
 * to the NavigationApplication from which this function was called.
 */
public fun NavigationApplication.createNavigationController(
    strictMode: Boolean = false,
    block: NavigationModuleScope.() -> Unit = {}
): NavigationController {
    if (this !is Application)
        throw IllegalArgumentException("A NavigationApplication must extend android.app.Application")

    val navigationController = NavigationController()
    navigationController.addModule(loadGeneratedNavigationModule())
    navigationController.addModule(createNavigationModule(block))
    return navigationController.apply {
        isStrictMode = strictMode
        install(this@createNavigationController)
    }
}

@Deprecated(
    message = "Please replace with [createNavigationController]",
    replaceWith = ReplaceWith("createNavigationController(strictMode, block)")
)
public fun NavigationApplication.navigationController(
    strictMode: Boolean = false,
    block: NavigationModuleScope.() -> Unit = {}
): NavigationController = createNavigationController(strictMode, block)


@Keep // Used by EnroTest
internal fun createUnattachedNavigationController(
    strictMode: Boolean = false,
    block: NavigationModuleScope.() -> Unit = {}
): NavigationController {
    val navigationController = NavigationController()
    navigationController.addModule(createNavigationModule(block))
    return navigationController.apply {
        isStrictMode = strictMode
    }
}

