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

// Applies the optional enro-compat module when `dev.enro.compat.EnroCompat` is on the classpath.
// enro-runtime has no compile-time dependency on enro-compat, so the class is loaded by
// reflection: the class name and the `compatModule` field name must match enro-compat.
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
