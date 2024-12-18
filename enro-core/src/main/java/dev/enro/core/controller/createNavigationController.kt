package dev.enro.core.controller

import android.app.Application
import androidx.annotation.Keep
import dev.enro.core.EnroConfig

/**
 * Create a NavigationController from the NavigationControllerDefinition/DSL, and immediately attach it
 * to the NavigationApplication from which this function was called.
 *
 * @param useLegacyContainerPresentBehavior see [EnroConfig.useLegacyContainerPresentBehavior]
 */
public fun NavigationApplication.createNavigationController(
    strictMode: Boolean = false,
    useLegacyContainerPresentBehavior: Boolean = false,
    backConfiguration: EnroBackConfiguration = EnroBackConfiguration.Default,
    block: NavigationModuleScope.() -> Unit = {}
): NavigationController {
    if (this !is Application)
        throw IllegalArgumentException("A NavigationApplication must extend android.app.Application")

    val navigationController = NavigationController()
    navigationController.addModule(loadGeneratedNavigationModule())
    navigationController.addModule(createNavigationModule(block))
    return navigationController.apply {
        setConfig(
            config.copy(
                isStrictMode = strictMode,
                useLegacyContainerPresentBehavior = useLegacyContainerPresentBehavior,
                backConfiguration = backConfiguration,
            )
        )
        install(this@createNavigationController)
    }
}

@Deprecated(
    message = "Please replace with [createNavigationController]",
    replaceWith = ReplaceWith("createNavigationController(strictMode, block)")
)
public fun NavigationApplication.navigationController(
    strictMode: Boolean = false,
    useLegacyContainerPresentBehavior: Boolean = false,
    backConfiguration: EnroBackConfiguration = EnroBackConfiguration.Default,
    block: NavigationModuleScope.() -> Unit = {}
): NavigationController = createNavigationController(
    strictMode = strictMode,
    useLegacyContainerPresentBehavior = useLegacyContainerPresentBehavior,
    backConfiguration = backConfiguration,
    block = block
)


@Keep // Used by EnroTest
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

