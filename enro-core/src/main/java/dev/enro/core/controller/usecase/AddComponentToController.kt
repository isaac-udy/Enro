package dev.enro.core.controller.usecase

import dev.enro.core.controller.NavigationComponentBuilder
import dev.enro.core.controller.interceptor.InstructionInterceptorRepository
import dev.enro.core.controller.repository.ComposeEnvironmentRepository
import dev.enro.core.controller.repository.ExecutorRepository
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.PluginRepository

internal class AddComponentToController(
    private val pluginRepository: PluginRepository,
    private val navigationBindingRepository: NavigationBindingRepository,
    private val executorRepository: ExecutorRepository,
    private val interceptorRepository: InstructionInterceptorRepository,
    private val composeEnvironmentRepository: ComposeEnvironmentRepository,
) {

    operator fun invoke(component: NavigationComponentBuilder) {
        pluginRepository.addPlugins(component.plugins)
        navigationBindingRepository.addNavigationBindings(component.bindings)
        executorRepository.addExecutors(component.overrides)
        interceptorRepository.addInterceptors(component.interceptors)

        component.composeEnvironment.let { environment ->
            if (environment == null) return@let
            composeEnvironmentRepository.setComposeEnvironment(environment)
        }
    }
}