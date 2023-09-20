package dev.enro.core.controller

import dev.enro.core.controller.repository.*
import dev.enro.core.controller.usecase.*
import dev.enro.core.controller.usecase.ComposeEnvironment
import dev.enro.core.result.EnroResult

internal class NavigationControllerScope(
    navigationController: NavigationController
)  : EnroDependencyScope {
    override val container: EnroDependencyContainer = EnroDependencyContainer(
        parentScope = null,
        registration = {
            register { navigationController }

            register { EnroResult() }

            // Repositories
            register { PluginRepository() }
            register { ClassHierarchyRepository() }
            register { NavigationBindingRepository() }
            register { ExecutorRepository(get()) }
            register { ComposeEnvironmentRepository() }
            register { InstructionInterceptorRepository() }
            register { NavigationAnimationRepository() }
            register { NavigationHostFactoryRepository(this) }

            // Usecases
            register { AddModuleToController(get(), get(), get(), get(), get(), get(), get()) }
            register { GetNavigationExecutor(get(), get()) }
            register { AddPendingResult(get(), get()) }
            register<ExecuteOpenInstruction> { ExecuteOpenInstructionImpl(get(), get(), get()) }
            register<ExecuteCloseInstruction> { ExecuteCloseInstructionImpl(get(), get(), get()) }
            register<ExecuteContainerOperationInstruction> { ExecuteContainerOperationInstructionImpl() }

            register { ConfigureNavigationHandleForPlugins(get()) }
            register { OnNavigationContextCreated(get(), get()) }
            register { OnNavigationContextSaved() }
            register { ComposeEnvironment(get()) }

            register { CanInstructionBeHostedAs(get(), get()) }
            register { HostInstructionAs(get(), get()) }
            register { GetNavigationBinding(get()) }
            register { GetNavigationAnimations(get(), get<NavigationAnimationRepository>().controllerOverrides) }
        }
    )
}