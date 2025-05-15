package dev.enro.core.controller

import androidx.savedstate.serialization.SavedStateConfiguration
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroConfig
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.NavigationPathRepository
import dev.enro.core.controller.repository.PluginRepository
import dev.enro.core.controller.repository.SerializerRepository
import dev.enro.core.controller.usecase.AddModuleToController
import dev.enro.core.path.ParsedPath
import dev.enro.core.plugins.EnroPlugin
import dev.enro.core.result.EnroResult
import dev.enro.core.window.NavigationWindowManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

public class NavigationController internal constructor() {
    internal val dependencyScope: EnroDependencyScope = NavigationControllerScope(this)
    private val enroResult: EnroResult = dependencyScope.get()
    private val pluginRepository: PluginRepository = dependencyScope.get()
    private val navigationBindingRepository: NavigationBindingRepository = dependencyScope.get()
    private val navigationPathRepository: NavigationPathRepository = dependencyScope.get()
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

    public fun addPlugin(plugin: EnroPlugin) {
        pluginRepository.addPlugins(listOf(plugin))
    }

    public fun removePlugin(plugin: EnroPlugin) {
        pluginRepository.removePlugins(listOf(plugin))
    }

    public fun instructionForPath(path: String): AnyOpenInstruction? {
        val parsedPath = ParsedPath.fromString(path)
        val pathBinding = navigationPathRepository.getPathBinding(parsedPath)
            ?: return null
        val navigationKey = pathBinding.fromPath(parsedPath)
        return NavigationInstruction.DefaultDirection(navigationKey)
    }

    public fun bindingForInstruction(
        instruction: AnyOpenInstruction,
    ): NavigationBinding<*, *>? {
        return navigationBindingRepository.bindingForInstruction(instruction)
    }

    // The reference parameter is used to pass the platform-specific reference to the NavigationController,
    // for example, the Application instance in Android, or the ApplicationScope instance on Desktop
    public fun install(reference: Any?) {
        if (navigationController == this) return
        if (navigationController != null) {
            error("A NavigationController is already installed")
        }
        navigationController = this
        platformReference = reference
        pluginRepository.onAttached(this)
    }

    // This method is called by the test module to install/uninstall Enro from test applications
    internal fun uninstall(application: Any) {
        pluginRepository.onDetached(this)
        if (navigationController == null) return
        require(navigationController == this) {
            "The currently installed NavigationController is not the same as the one being uninstalled"
        }
        navigationController = null
        platformReference = null
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
        internal var navigationController: NavigationController? = null
            private set

        internal var platformReference: Any? = null
            private set

        /**
         * SerializersModule is used to configure serialization internally within Enro, and is
         * based on the serializers that have been registered with Enro's NavigationController.
         * This is the default serializers module that is used for serialization within Enro,
         * and is used by the savedStateConfiguration and jsonConfiguration.
         */
        @AdvancedEnroApi
        public val serializersModule: SerializersModule
            get() {
                val activeController = requireNotNull(navigationController) {
                    "Could not find active NavigationController"
                }
                return activeController.dependencyScope.get<SerializerRepository>()
                    .serializersModule
            }

        /**
         * SavedStateConfiguration is used to configure the androidx.savedstate serialization for
         * Enro, based on the serializers that have been registered with Enro; this is primarily
         * for internal use when managing serialization internally within Enro, but is exposed
         * for advanced use cases where you may need to perform some custom serialization.
         */
        @AdvancedEnroApi
        public val savedStateConfiguration: SavedStateConfiguration
            get() {
                val activeController = requireNotNull(navigationController) {
                    "Could not find active NavigationController"
                }
                return activeController.dependencyScope.get<SerializerRepository>()
                    .getSavedStateConfiguration()
            }

        /**
         * JsonConfiguration is used to configure the kotlinx.serialization.Json serialization for
         * Enro, based on the serializers that have been registered with Enro; this is primarily
         * for internal use when managing serialization internally within Enro, but is exposed
         * for advanced use cases where you may need to perform some custom serialization.
         */
        @AdvancedEnroApi
        public val jsonConfiguration: Json
            get() {
                val activeController = requireNotNull(navigationController) {
                    "Could not find active NavigationController"
                }
                return activeController.dependencyScope.get<SerializerRepository>()
                    .getJsonConfiguration()
            }
    }
}

