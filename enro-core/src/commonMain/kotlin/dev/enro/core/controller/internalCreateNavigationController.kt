package dev.enro.core.controller

// Marked as internal, but is used in generated code with a @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
internal fun internalCreateNavigationController(
    strictMode: Boolean = false,
    useLegacyContainerPresentBehavior: Boolean = false,
    backConfiguration: EnroBackConfiguration = EnroBackConfiguration.Default,
    block: NavigationModuleScope.() -> Unit = {}
): NavigationController {
    val navigationController = NavigationController()
    navigationController.addModule(createNavigationModule(block))
    return navigationController.apply {
        setConfig(
            config.copy(
                isStrictMode = strictMode,
                useLegacyContainerPresentBehavior = useLegacyContainerPresentBehavior,
                backConfiguration = backConfiguration,
            )
        )
    }
}

internal fun createUnattachedNavigationController(
    strictMode: Boolean = false,
    useLegacyContainerPresentBehavior: Boolean = false,
    backConfiguration: EnroBackConfiguration = EnroBackConfiguration.Default,
    block: NavigationModuleScope.() -> Unit = {}
): NavigationController {
    val navigationController = NavigationController()
    navigationController.addModule(createNavigationModule(block))
    return navigationController.apply {
        setConfig(
            config.copy(
                isStrictMode = strictMode,
                useLegacyContainerPresentBehavior = useLegacyContainerPresentBehavior,
                backConfiguration = backConfiguration,
            )
        )
    }
}
