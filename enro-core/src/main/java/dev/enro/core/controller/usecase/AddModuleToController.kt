package dev.enro.core.controller.usecase

import dev.enro.core.controller.NavigationModule
import dev.enro.core.controller.repository.*

internal class AddModuleToController(
    private val pluginRepository: PluginRepository,
    private val navigationBindingRepository: NavigationBindingRepository,
    private val executorRepository: ExecutorRepository,
    private val interceptorRepository: InstructionInterceptorRepository,
    private val composeEnvironmentRepository: ComposeEnvironmentRepository,
    private val navigationHostFactoryRepository: NavigationHostFactoryRepository,
) {

    operator fun invoke(module: NavigationModule) {
        pluginRepository.addPlugins(module.plugins)
        navigationBindingRepository.addNavigationBindings(module.bindings)
        executorRepository.addExecutors(module.overrides)
        interceptorRepository.addInterceptors(module.interceptors)

        module.hostFactories.forEach { navigationHostFactoryRepository.addFactory(it) }

        module.composeEnvironment.let { environment ->
            if (environment == null) return@let
            composeEnvironmentRepository.setComposeEnvironment(environment)
        }
    }
}