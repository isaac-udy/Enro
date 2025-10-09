package dev.enro.platform

import dev.enro.NavigationKey
import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal actual val platformNavigationModule: NavigationModule = createNavigationModule {
    applyCompatModule()
    plugin(ActivityPlugin)
    serializersModule(SerializersModule {
        polymorphic(Any::class) {
            subclass(DefaultActivityNavigationKey::class)
        }
        polymorphic(NavigationKey::class) {
            subclass(DefaultActivityNavigationKey::class)
        }
    })
}

/**
 * Attempts to load and apply the optional enro-compat module if it is present on the classpath.
 *
 * This function uses reflection to check for the presence of the `dev.enro.compat.EnroCompat` class
 * at runtime. If found, it instantiates the class and retrieves its `compatModule` field, which
 * contains compatibility-related navigation functionality.
 *
 * The enro-compat module is optional and provides backward compatibility support for applications
 * migrating from older versions of Enro. By loading it dynamically at runtime, applications can
 * include the compatibility module as a dependency only when needed, without requiring it as a
 * compile-time dependency of the core module.
 *
 * If the compat module is successfully loaded, a warning log is emitted to inform developers that
 * the compatibility layer is active.
 */
private fun NavigationModule.BuilderScope.applyCompatModule() {
    val compatClass = runCatching { Class.forName("dev.enro.compat.EnroCompat") }
        .getOrNull() ?: return

    val compat = compatClass.constructors.first().newInstance()
    val compatModule = compatClass.declaredFields
        .first { it.name == "compatModule" }
        .get(compat)

    require(compatModule is NavigationModule)
    module(compatModule)
    EnroLog.error("The enro-compat module is active. This is not recommended for new applications. Please migrate to the new API as soon as possible, enro-compat will be removed in a future release.")
}
