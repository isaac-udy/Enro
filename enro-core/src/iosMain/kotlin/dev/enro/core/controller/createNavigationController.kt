package dev.enro.core.controller

import platform.UIKit.UIApplication


public fun createNavigationController(
    application: UIApplication,
    strictMode: Boolean = false,
    useLegacyContainerPresentBehavior: Boolean = false,
    backConfiguration: EnroBackConfiguration = EnroBackConfiguration.Default,
    block: NavigationModuleScope.() -> Unit = {}
): NavigationController {
    val navigationController = NavigationController()
    // TODO: Generated module!
//        navigationController.addModule(loadGeneratedNavigationModule())
    navigationController.addModule(createNavigationModule(block))
    return navigationController.apply {
        setConfig(
            config.copy(
                isStrictMode = strictMode,
                useLegacyContainerPresentBehavior = useLegacyContainerPresentBehavior,
                backConfiguration = backConfiguration,
            )
        )
        install(application)
    }
}