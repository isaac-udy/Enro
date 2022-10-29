package dev.enro.core.controller

import android.app.Application
import android.os.Bundle
import androidx.annotation.Keep
import dev.enro.core.EnroException
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationExecutor
import dev.enro.core.NavigationKey
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.controller.interceptor.InstructionInterceptorRepository
import dev.enro.core.controller.lifecycle.NavigationLifecycleController
import dev.enro.core.controller.repository.*
import dev.enro.core.internal.get
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.result.EnroResult
import kotlin.reflect.KClass

public class NavigationController internal constructor() {
    internal var isInTest = false

    internal var isStrictMode: Boolean = false

    internal val dependencyScope = NavigationControllerScope(this)

    private val enroResult: EnroResult = dependencyScope.get()
    private val pluginRepository: PluginRepository = dependencyScope.get()
    private val classHierarchyRepository: ClassHierarchyRepository = dependencyScope.get()
    private val navigationBindingRepository: NavigationBindingRepository = dependencyScope.get()
    private val executorRepository: ExecutorRepository = dependencyScope.get()
    private val composeEnvironmentRepository: ComposeEnvironmentRepository = dependencyScope.get()
    private val interceptorRepository: InstructionInterceptorRepository = dependencyScope.get()
    private val contextController: NavigationLifecycleController = dependencyScope.get()

    init {
        pluginRepository.addPlugins(listOf(enroResult))
        addComponent(defaultComponent)
    }

    public fun addComponent(component: NavigationComponentBuilder) {
        pluginRepository.addPlugins(component.plugins)
        navigationBindingRepository.addNavigationBindings(component.bindings)
        executorRepository.addExecutors(component.overrides)
        interceptorRepository.addInterceptors(component.interceptors)

        component.composeEnvironment.let { environment ->
            if (environment == null) return@let
            composeEnvironmentRepository.setComposeEnvironment(environment)
        }
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
    // This method is called by the test module to install/uninstall Enro from test applications
    internal fun installForJvmTests() {
        pluginRepository.onAttached(this)
    }

    @Keep
    // This method is called by the test module to install/uninstall Enro from test applications
    internal fun uninstall(application: Application) {
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