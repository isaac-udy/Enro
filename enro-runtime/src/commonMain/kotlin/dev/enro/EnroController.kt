package dev.enro

import androidx.compose.runtime.Stable
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.context.RootContextRegistry
import dev.enro.controller.NavigationModule
import dev.enro.controller.repository.BindingRepository
import dev.enro.controller.repository.DecoratorRepository
import dev.enro.controller.repository.InterceptorRepository
import dev.enro.controller.repository.PathRepository
import dev.enro.controller.repository.PluginRepository
import dev.enro.controller.repository.SerializerRepository
import dev.enro.controller.repository.ViewModelRepository
import dev.enro.serialization.wrapForSerialization
import kotlinx.serialization.json.Json

@Stable
public class EnroController {
    // TODO NEED TO CONFIGURE THIS
    internal val isDebug = false
    internal var platformReference: Any? = null
    internal val plugins = PluginRepository()
    internal val bindings = BindingRepository(plugins)
    internal val serializers = SerializerRepository()
    internal val interceptors = InterceptorRepository()
    internal val decorators = DecoratorRepository()
    internal val paths = PathRepository()
    internal val viewModelRepository = ViewModelRepository()

    internal val rootContextRegistry: RootContextRegistry = RootContextRegistry()

    /**
     * Attaches a module to the EnroController *after* the Controller has been attached;
     * you can't uninstall a module once it has been added, use with caution.
     */
    internal fun addModule(module: NavigationModule) {
        plugins.addPlugins(module.plugins)
        bindings.addNavigationBindings(module.bindings)
        interceptors.addInterceptors(module.interceptors)
        paths.addPaths(module.paths)
        decorators.addDecorators(module.decorators)
        serializers.registerSerializersModule(module.serializers)
        serializers.registerSerializersModule(module.serializersForBindings)
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
        init {
            @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
            NavigationKey.verifyMetadataSerialization = verifyMetadataSerialization@ { key, value ->
                val controller = requireInstance()
                if (!controller.isDebug) return@verifyMetadataSerialization
                val isTransient = key is NavigationKey.TransientMetadataKey
                if (isTransient) return@verifyMetadataSerialization
                if (value != null) {
                    val wrapped = value.wrapForSerialization()
                    val hasSerializer = controller.serializers.serializersModule.getPolymorphic(Any::class, wrapped) != null
                    if (!hasSerializer) {
                        error("Object of type ${value::class} could not be added to NavigationKey.Metadata, make sure to register the serializer with the NavigationController.")
                    }
                }
            }
        }

        internal var instance: EnroController? = null
            private set

        internal fun requireInstance(): EnroController {
            return instance ?: error("EnroController has not been installed")
        }

        public val jsonConfiguration: Json get() {
            val instance = requireInstance()
            return instance.serializers.jsonConfiguration
        }

        public val savedStateConfiguration: SavedStateConfiguration get() {
            val instance = requireInstance()
            return instance.serializers.savedStateConfiguration
        }
    }
}
