package dev.enro.core.controller

import dev.enro.core.controller.interceptor.InstructionInterceptorRepository
import dev.enro.core.controller.lifecycle.NavigationLifecycleController
import dev.enro.core.controller.repository.*
import dev.enro.core.controller.usecase.*
import dev.enro.core.internal.EnroDependencyContainer
import dev.enro.core.internal.EnroDependencyScope
import dev.enro.core.internal.get
import dev.enro.core.internal.register
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
            register { NavigationLifecycleController(get()) }

            // Usecases
            register { AddComponentToController(get(), get(), get(), get(), get()) }
            register { GetNavigationExecutor(get(), get()) }
            register { AddPendingResult(get()) }
            register<ExecuteOpenInstruction> { ExecuteOpenInstructionImpl(get(), get(), get()) }
            register<ExecuteCloseInstruction> { ExecuteCloseInstructionImpl(get(), get(), get()) }
        }
    )
}