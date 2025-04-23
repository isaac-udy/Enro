package dev.enro.core.controller

import org.w3c.dom.Document

public fun createNavigationController(
    document: Document,
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
        install(document)
    }
}