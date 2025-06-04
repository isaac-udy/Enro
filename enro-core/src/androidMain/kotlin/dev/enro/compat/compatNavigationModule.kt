package dev.enro.compat

import dev.enro.controller.createNavigationModule
import dev.enro.core.NavigationDirection
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val compatNavigationModule = createNavigationModule {
    serializersModule(SerializersModule {
        polymorphic(Any::class) {
            subclass(NavigationDirection.Push::class)
            subclass(NavigationDirection.Present::class)
        }
    })
}