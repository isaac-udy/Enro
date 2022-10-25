package dev.enro.core.controller

import android.app.Application
import android.os.Bundle
import androidx.annotation.Keep
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.controller.interceptor.InstructionInterceptorRepository
import dev.enro.core.controller.lifecycle.NavigationLifecycleController
import dev.enro.core.controller.repository.*
import dev.enro.core.internal.handle.NavigationHandleViewModel
import kotlin.reflect.KClass

public class NavigationController internal constructor() {
    internal var isInTest = false

    internal var isStrictMode: Boolean = false

    private val pluginRepository: PluginRepository = PluginRepository()
    private val classHierarchyRepository: ClassHierarchyRepository = ClassHierarchyRepository()
    private val navigationBindingRepository: NavigationBindingRepository =
        NavigationBindingRepository()
    private val executorRepository: ExecutorRepository =
        ExecutorRepository(classHierarchyRepository)
    internal val composeEnvironmentRepository: ComposeEnvironmentRepository =
        ComposeEnvironmentRepository()
    private val interceptorContainer: InstructionInterceptorRepository =
        InstructionInterceptorRepository()
    private val contextController: NavigationLifecycleController =
        NavigationLifecycleController(executorRepository, pluginRepository)

    init {
        addComponent(defaultComponent)
    }

    public fun addComponent(component: NavigationComponentBuilder) {
        pluginRepository.addPlugins(component.plugins)
        navigationBindingRepository.addNavigationBindings(component.bindings)
        executorRepository.addExecutors(component.overrides)
        interceptorContainer.addInterceptors(component.interceptors)

        component.composeEnvironment.let { environment ->
            if (environment == null) return@let
            composeEnvironmentRepository.setComposeEnvironment(environment)
        }
    }

    internal fun open(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    ) {
        val binding = bindingForKeyType(instruction.navigationKey::class)
            ?: throw EnroException.MissingNavigationBinding("Attempted to execute $instruction but could not find a valid navigation binding for the key type on this instruction")

        val processedInstruction = interceptorContainer.intercept(
            instruction, navigationContext, binding
        ) ?: return

        if (processedInstruction.navigationKey::class != binding.keyType) {
            navigationContext.getNavigationHandle().executeInstruction(processedInstruction)
            return
        }
        val executor =
            executorRepository.executorFor(processedInstruction.internal.openedByType to processedInstruction.internal.openingType)

        val args = ExecutorArgs(
            navigationContext,
            binding,
            processedInstruction.navigationKey,
            processedInstruction
        )

        executor.preOpened(navigationContext)
        executor.open(args)
    }

    internal fun close(
        navigationContext: NavigationContext<out Any>
    ) {
        val processedInstruction = interceptorContainer.intercept(
            NavigationInstruction.Close, navigationContext
        ) ?: return

        if (processedInstruction !is NavigationInstruction.Close) {
            navigationContext.getNavigationHandle().executeInstruction(processedInstruction)
            return
        }
        val executor: NavigationExecutor<Any, Any, NavigationKey> =
            executorRepository.executorFor(navigationContext.getNavigationHandle().instruction.internal.openedByType to navigationContext.contextReference::class.java)
        executor.preClosed(navigationContext)
        executor.close(navigationContext)
    }

    public fun bindingForDestinationType(
        destinationType: KClass<*>
    ): NavigationBinding<*, *>? {
        return navigationBindingRepository.bindingForDestinationType(destinationType)
    }

    public fun bindingForKeyType(
        keyType: KClass<out NavigationKey>
    ): NavigationBinding<*, *>? {
        return navigationBindingRepository.bindingForKeyType(keyType)
    }

    internal fun executorForOpen(instruction: AnyOpenInstruction) =
        executorRepository.executorFor(instruction.internal.openedByType to instruction.internal.openingType)

    internal fun executorForClose(navigationContext: NavigationContext<*>) =
        executorRepository.executorFor(navigationContext.getNavigationHandle().instruction.internal.openedByType to navigationContext.contextReference::class.java)

    public fun addOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        executorRepository.addExecutorOverride(navigationExecutor)
    }

    public fun removeOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        executorRepository.removeExecutorOverride(navigationExecutor)
    }

    public fun install(application: Application) {
        navigationControllerBindings[application] = this
        contextController.install(application)
        pluginRepository.onAttached(this)
    }

    @Keep
    // This method is called reflectively by the test module to install/uninstall Enro from test applications
    private fun installForJvmTests() {
        pluginRepository.onAttached(this)
    }

    @Keep
    // This method is called reflectively by the test module to install/uninstall Enro from test applications
    private fun uninstall(application: Application) {
        navigationControllerBindings.remove(application)
        contextController.uninstall(application)
    }

    internal fun onComposeDestinationAttached(
        destination: ComposableDestination,
        savedInstanceState: Bundle?
    ): NavigationHandleViewModel {
        return contextController.onContextCreated(
            destination.context,
            savedInstanceState
        )
    }

    internal fun onComposeContextSaved(destination: ComposableDestination, outState: Bundle) {
        contextController.onContextSaved(
            destination.context,
            outState
        )
    }

    public companion object {
        internal val navigationControllerBindings =
            mutableMapOf<Application, NavigationController>()
    }
}

public val Application.navigationController: NavigationController
    get() {
        synchronized(this) {
            if (this is NavigationApplication) return navigationController
            val bound = NavigationController.navigationControllerBindings[this]
            if (bound == null) {
                val navigationController = NavigationController()
                NavigationController.navigationControllerBindings[this] = NavigationController()
                navigationController.install(this)
                return navigationController
            }
            return bound
        }
    }

internal val NavigationController.application: Application
    get() {
        return NavigationController.navigationControllerBindings.entries
            .firstOrNull {
                it.value == this
            }
            ?.key
            ?: throw EnroException.NavigationControllerIsNotAttached("NavigationController is not attached to an Application")
    }