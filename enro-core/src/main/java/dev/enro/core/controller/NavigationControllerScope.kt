package dev.enro.core.controller

import android.app.Application
import androidx.fragment.app.FragmentManager
import dev.enro.core.*
import dev.enro.core.controller.lifecycle.ActivityLifecycleCallbacksForEnro
import dev.enro.core.controller.lifecycle.FragmentLifecycleCallbacksForEnro
import dev.enro.core.controller.repository.*
import dev.enro.core.controller.usecase.*
import dev.enro.core.result.internal.EnroResult
import dev.enro.core.usecase.*

@ArchitectureException("This binds internal implementations into the public interface, so is allowed to access the internal implementations")
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

            // Usecases
            register { AddComponentToController(get(), get(), get(), get(), get()) }
            register { ConfigureNavigationHandleForPlugins(get()) }

            register<GetNavigationExecutor> { GetNavigationExecutorImpl(get(), get()) }
            register<AddPendingResult> { AddPendingResultImpl(get()) }
            register<ExecuteOpenInstruction> { ExecuteOpenInstructionImpl(get(), get(), get()) }
            register<ExecuteCloseInstruction> { ExecuteCloseInstructionImpl(get(), get(), get()) }
            register<OnNavigationContextCreated> { OnNavigationContextCreatedImpl(get(), get()) }
            register<OnNavigationContextSaved> { OnNavigationContextSavedImpl() }
            register<RenderComposableInEnroEnvironment> { RenderComposableInEnroEnvironmentImpl(get()) }
            register<ConfigureNavigationHandle> { ConfigureNavigationHandleImpl() }
            register<GetContextFromNavigationHandle> { GetContextFromNavigationHandleImpl() }
            register<EnsureNavigationInstructionHasContext> { EnsureNavigationInstructionHasContextImpl() }

            // Other
            register<Application.ActivityLifecycleCallbacks> { ActivityLifecycleCallbacksForEnro(get(), get(), get()) }
            register<FragmentManager.FragmentLifecycleCallbacks> { FragmentLifecycleCallbacksForEnro(get(), get()) }
        }
    )
}