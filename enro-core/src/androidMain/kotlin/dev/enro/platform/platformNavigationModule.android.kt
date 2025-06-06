package dev.enro.platform

import dev.enro.NavigationKey
import dev.enro.compat.compatNavigationModule
import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal actual val platformNavigationModule: NavigationModule = createNavigationModule {
    module(compatNavigationModule)
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
