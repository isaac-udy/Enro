package dev.enro.core.controller

import dev.enro.core.EnroConfig
import dev.enro.core.controller.repository.ComposeEnvironmentRepository
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.controller.repository.NavigationAnimationRepository
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.NavigationHostFactoryRepository
import dev.enro.core.controller.repository.PluginRepository
import dev.enro.core.controller.repository.SerializerRepository
import dev.enro.core.controller.usecase.ActiveNavigationHandleReference
import dev.enro.core.controller.usecase.AddModuleToController
import dev.enro.core.controller.usecase.AddPendingResult
import dev.enro.core.controller.usecase.CanInstructionBeHostedAs
import dev.enro.core.controller.usecase.ComposeEnvironment
import dev.enro.core.controller.usecase.ExecuteCloseInstruction
import dev.enro.core.controller.usecase.ExecuteCloseInstructionImpl
import dev.enro.core.controller.usecase.ExecuteContainerOperationInstruction
import dev.enro.core.controller.usecase.ExecuteContainerOperationInstructionImpl
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.controller.usecase.ExecuteOpenInstructionImpl
import dev.enro.core.controller.usecase.GetAnimationsForTransition
import dev.enro.core.controller.usecase.GetNavigationAnimations
import dev.enro.core.controller.usecase.GetNavigationBinding
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.result.EnroResult
import dev.enro.core.window.NavigationWindowManager

internal class NavigationControllerScope(
    navigationController: NavigationController
)  : EnroDependencyScope {
    override val container: EnroDependencyContainer = EnroDependencyContainer(
        parentScope = null,
        registration = {
            register { navigationController }
            register<EnroConfig> { navigationController.config }

            register { EnroResult() }

            // Repositories
            register { PluginRepository() }
            register { NavigationBindingRepository() }
            register { ComposeEnvironmentRepository() }
            register { InstructionInterceptorRepository() }
            register { NavigationAnimationRepository() }
            register { NavigationHostFactoryRepository() }
            register { SerializerRepository() }

            // Usecases
            register { AddModuleToController(get(), get(), get(), get(), get(), get(), get()) }
            register { AddPendingResult(get(), get()) }
            register<ExecuteOpenInstruction> { ExecuteOpenInstructionImpl(get(), get()) }
            register<ExecuteCloseInstruction> { ExecuteCloseInstructionImpl(get(), get()) }
            register<ExecuteContainerOperationInstruction> { ExecuteContainerOperationInstructionImpl() }

            register { ActiveNavigationHandleReference(get()) }
            register { OnNavigationContextCreated(get()) }
            register { OnNavigationContextSaved() }
            register { ComposeEnvironment(get()) }

            register { CanInstructionBeHostedAs(get(), get()) }
            register { HostInstructionAs(get(), get()) }
            register { GetNavigationBinding(get()) }
            register { GetNavigationAnimations(get(), get<NavigationAnimationRepository>().controllerOverrides) }
            register { GetAnimationsForTransition() }

            register {
                NavigationWindowManager(get())
                    .also {
                        get<PluginRepository>().addPlugins(listOf(it))
                    }
            }
        }
    )
}