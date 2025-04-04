package dev.enro.core.controller.usecase

import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.controller.NavigationModule
import dev.enro.core.controller.repository.ComposeEnvironmentRepository
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.controller.repository.NavigationAnimationRepository
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.NavigationHostFactoryRepository
import dev.enro.core.controller.repository.PluginRepository

// The following @OptIn shouldn't be required due to buildSrc/src/main/kotlin/configureAndroid.kt adding an -Xopt-in arg
// to the Kotlin freeCompilerArgs, but for some reason, lint checks will fail if the @OptIn annotation is not explicitly added.
@OptIn(AdvancedEnroApi::class)
internal class AddModuleToController(
    private val pluginRepository: PluginRepository,
    private val navigationBindingRepository: NavigationBindingRepository,
    private val interceptorRepository: InstructionInterceptorRepository,
    private val animationRepository: NavigationAnimationRepository,
    private val composeEnvironmentRepository: ComposeEnvironmentRepository,
    private val navigationHostFactoryRepository: NavigationHostFactoryRepository,
) {

    operator fun invoke(module: NavigationModule) {
        pluginRepository.addPlugins(module.plugins)
        navigationBindingRepository.addNavigationBindings(module.bindings)
        interceptorRepository.addInterceptors(module.interceptors)
        module.animations.forEach { animationRepository.addAnimations(it) }
        module.hostFactories.forEach { navigationHostFactoryRepository.addFactory(it) }

        module.composeEnvironment.let { environment ->
            if (environment == null) return@let
            composeEnvironmentRepository.setComposeEnvironment(environment)
        }
    }
}