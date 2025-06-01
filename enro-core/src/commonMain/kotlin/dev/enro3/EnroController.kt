package dev.enro3

import dev.enro3.controller.NavigationModule
import dev.enro3.controller.repository.*

public class EnroController {
    // TODO NEED TO CONFIGURE THIS
    internal val isDebug = false

    internal val plugins = PluginRepository()
    internal val bindings = BindingRepository()
    internal val serializers = SerializerRepository()
    internal val interceptors = InterceptorRepository()
    internal val paths = PathRepository()

    internal fun addModule(module: NavigationModule) {
        plugins.addPlugins(module.plugins)
        bindings.addNavigationBindings(module.bindings)
        interceptors.addInterceptors(module.interceptors)
        paths.addPaths(module.paths)
        serializers.registerSerializersModule(module.serializers)
    }

    // The reference parameter is used to pass the platform-specific reference to the NavigationController,
    // for example, the Application instance in Android, or the ApplicationScope instance on Desktop
    public fun install(reference: Any?) {
        if (instance == this) return
        require (instance == null) {
            "A NavigationController is already installed"
        }
        instance = this
        platformReference = reference
        plugins.onAttached(this)
    }

    // This method is called by the test module to install/uninstall Enro from test applications
    internal fun uninstall() {
        plugins.onDetached(this)
        if (instance == null) return
        require(instance == this) {
            "The currently installed NavigationController is not the same as the one being uninstalled"
        }
        instance = null
        platformReference = null
    }

    public companion object {
        internal var instance: EnroController? = null
            private set

        internal fun requireInstance(): EnroController {
            return instance ?: error("EnroController has not been installed")
        }

        internal var platformReference: Any? = null
            private set
    }
}