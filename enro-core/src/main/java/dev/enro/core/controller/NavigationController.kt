package dev.enro.core.controller

import android.app.Application
import androidx.annotation.Keep
import dev.enro.core.EnroConfig
import dev.enro.core.EnroException
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationExecutor
import dev.enro.core.NavigationKey
import dev.enro.core.controller.repository.ExecutorRepository
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.PluginRepository
import dev.enro.core.controller.usecase.AddModuleToController
import dev.enro.core.result.EnroResult
import kotlin.reflect.KClass

public class NavigationController internal constructor() {
    internal val dependencyScope = NavigationControllerScope(this)

    private val enroResult: EnroResult = dependencyScope.get()
    private val pluginRepository: PluginRepository = dependencyScope.get()
    private val navigationBindingRepository: NavigationBindingRepository = dependencyScope.get()
    private val executorRepository: ExecutorRepository = dependencyScope.get()
    private val addModuleToController: AddModuleToController = dependencyScope.get()

    public var config: EnroConfig = EnroConfig()
        private set

    init {
        pluginRepository.addPlugins(listOf(enroResult))
        addModule(defaultNavigationModule)
    }

    public fun addModule(component: NavigationModule) {
        addModuleToController(component)
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
        pluginRepository.onDetached(this)
        navigationControllerBindings.remove(application)
    }

    /**
     * This method is used to set the config, instead of using "internal set" on the config variable, because we
     * want to be able to use this method from inside the test module, which needs to use @Suppress for
     * "INVISIBLE_REFERENCE" and "INVISIBLE_MEMBER" to access internal functionality, and it appears that this does not
     * allow access to set variables declared as "internal set"
     */
    internal fun setConfig(config: EnroConfig) {
        this.config = config
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

public val NavigationController.isInAndroidContext: Boolean
    get() = NavigationController.navigationControllerBindings.isNotEmpty()

internal val NavigationController.application: Application
    get() {
        return NavigationController.navigationControllerBindings.entries
            .firstOrNull {
                it.value == this
            }
            ?.key
            ?: throw EnroException.NavigationControllerIsNotAttached("NavigationController is not attached to an Application")
    }