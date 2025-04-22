package dev.enro.core.controller

import dev.enro.core.EnroConfig
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.PluginRepository
import dev.enro.core.controller.repository.SerializerRepository
import dev.enro.core.controller.usecase.AddModuleToController
import dev.enro.core.result.EnroResult
import dev.enro.core.window.NavigationWindowManager
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

public class NavigationController internal constructor() {
    internal val dependencyScope: EnroDependencyScope = NavigationControllerScope(this)

    private val enroResult: EnroResult = dependencyScope.get()
    private val pluginRepository: PluginRepository = dependencyScope.get()
    private val navigationBindingRepository: NavigationBindingRepository = dependencyScope.get()
    private val addModuleToController: AddModuleToController = dependencyScope.get()
    public val windowManager: NavigationWindowManager = dependencyScope.get()

    internal var config: EnroConfig = EnroConfig()
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

    // TODO do we need to bother with the install/uninstall stuff, or should
    // we only allow a single controller at a time and throw otherwise?
    public fun install(application: NavigationApplication) {
        navigationControllerBindings[application] = this
        pluginRepository.onAttached(this)
    }

    // This method is called by the test module to install/uninstall Enro from test applications
    internal fun installForJvmTests() {
        navigationControllerBindings[Unit] = this
        pluginRepository.onAttached(this)
    }

    // This method is called by the test module to install/uninstall Enro from test applications
    internal fun installForAny(application: Any) {
        navigationControllerBindings[application] = this
        pluginRepository.onAttached(this)
    }

    // This method is called by the test module to install/uninstall Enro from test applications
    internal fun uninstall(application: Any) {
        pluginRepository.onDetached(this)
        navigationControllerBindings.remove(application)
        navigationControllerBindings.remove(Unit)
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
            mutableMapOf<Any, NavigationController>()

        @PublishedApi
        internal val serializersModule: SerializersModule
            get() {
                return SerializersModule {
                    navigationControllerBindings.values
                        .map {
                            it.dependencyScope.get<SerializerRepository>()
                                .serializersModule
                        }
                        .forEach {
                            include(it)
                        }
                }
            }
    }
}

