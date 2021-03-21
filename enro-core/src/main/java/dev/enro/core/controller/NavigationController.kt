package dev.enro.core.controller

import android.app.Application
import dev.enro.core.*
import dev.enro.core.controller.container.ExecutorContainer
import dev.enro.core.controller.container.NavigatorContainer
import dev.enro.core.controller.container.PluginContainer
import dev.enro.core.controller.interceptor.InstructionInterceptorController
import dev.enro.core.controller.lifecycle.NavigationLifecycleController
import kotlin.reflect.KClass

class NavigationController internal constructor(
    private val pluginContainer: PluginContainer,
    private val navigatorContainer: NavigatorContainer,
    private val executorContainer: ExecutorContainer,
    private val interceptorController: InstructionInterceptorController,
    private val contextController: NavigationLifecycleController,
) {
    internal var isInTest = false

    init {
        pluginContainer.onAttached(this)
    }

    internal fun open(
        navigationContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Open
    ) {
        val navigator = navigatorForKeyType(instruction.navigationKey::class)
            ?: throw IllegalStateException("Attempted to execute $instruction but could not find a valid navigator for the key type on this instruction")

        val executor = executorContainer.executorForOpen(navigationContext, navigator)

        val processedInstruction = interceptorController.intercept(
            instruction, executor.context, navigator
        )

        if (processedInstruction.navigationKey::class != navigator.keyType) {
            open(navigationContext, processedInstruction)
            return
        }

        val args = ExecutorArgs(
            executor.context,
            navigator,
            processedInstruction.navigationKey,
            processedInstruction
        )

        executor.executor.preOpened(executor.context)
        executor.executor.open(args)
    }

    internal fun close(
        navigationContext: NavigationContext<out Any>
    ) {
        val executor = executorContainer.executorForClose(navigationContext)
        executor.preClosed(navigationContext)
        executor.close(navigationContext)
    }

    fun navigatorForContextType(
        contextType: KClass<*>
    ): Navigator<*, *>? {
        return navigatorContainer.navigatorForContextType(contextType)
    }

    fun navigatorForKeyType(
        keyType: KClass<out NavigationKey>
    ): Navigator<*, *>? {
        return navigatorContainer.navigatorForKeyType(keyType)
    }

    internal fun executorForOpen(
        fromContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open
    ) = executorContainer.executorForOpen(
        fromContext,
        navigatorForKeyType(instruction.navigationKey::class) ?: throw IllegalStateException()
    )

    internal fun executorForClose(navigationContext: NavigationContext<*>) =
        executorContainer.executorForClose(navigationContext)

    fun addOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        executorContainer.addOverride(navigationExecutor)
    }

    fun removeOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        executorContainer.removeOverride(navigationExecutor)
    }

    fun install(navigationApplication: NavigationApplication) {
        if (navigationApplication !is Application)
            throw IllegalArgumentException("A NavigationApplication must extend android.app.Application")

        navigationControllerBindings[navigationApplication] = this
        contextController.install(navigationApplication)
    }

    private fun installForTest(application: Application) {
        navigationControllerBindings[application] = this
        contextController.install(application)
    }

    private fun uninstall(application: Application) {
        navigationControllerBindings.remove(application)
        contextController.uninstall(application)
    }

    companion object {
        internal val navigationControllerBindings = mutableMapOf<Application, NavigationController>()

        private fun getBoundApplicationForTest(application: Application) = navigationControllerBindings[application]
    }
}

val Application.navigationController: NavigationController get() {
    if(this is NavigationApplication) return navigationController
    val bound = NavigationController.navigationControllerBindings[this]
    if(bound != null) return bound
    throw IllegalStateException("Application is not a NavigationApplication, and has no attached NavigationController ")
}