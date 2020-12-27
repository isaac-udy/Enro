package nav.enro.core.controller

import android.app.Application
import nav.enro.core.*
import nav.enro.core.controller.container.ExecutorContainer
import nav.enro.core.controller.container.NavigatorContainer
import nav.enro.core.controller.container.PluginContainer
import nav.enro.core.controller.interceptor.InstructionInterceptorController
import nav.enro.core.controller.lifecycle.NavigationLifecycleController
import kotlin.reflect.KClass

class NavigationController internal constructor(
    private val pluginContainer: PluginContainer,
    private val navigatorContainer: NavigatorContainer,
    private val executorContainer: ExecutorContainer,
    private val interceptorController: InstructionInterceptorController,
    private val contextController: NavigationLifecycleController,
) {

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

        if(navigationApplication.navigationController != this)
            throw IllegalArgumentException("A NavigationController can only be installed on a NavigationApplication that returns that NavigationController as its navigationController property")

        contextController.install(navigationApplication)
    }
}