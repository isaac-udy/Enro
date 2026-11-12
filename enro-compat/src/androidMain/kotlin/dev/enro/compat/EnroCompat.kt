package dev.enro.compat

import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import dev.enro.core.NavigationDirection
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Compatibility support for applications that migrate from the Enro 2.x APIs to the
 * Enro 3.x APIs.
 *
 * Enro's Android platform module looks up EnroCompat by class name at runtime. When
 * EnroCompat is on the classpath, the platform module creates an EnroCompat and registers
 * [compatModule].
 *
 * [compatModule] registers serializers for [NavigationDirection], and a plugin that displays
 * a destination opened with [NavigationDirection.Present] as a direct overlay.
 */
public class EnroCompat {
    @JvmField
    public val compatModule: NavigationModule = createNavigationModule {
        plugin(LegacyNavigationDirectionPlugin)
        serializersModule(SerializersModule {
            polymorphic(Any::class) {
                subclass(NavigationDirection.Push::class)
                subclass(NavigationDirection.Present::class)
            }
        })
    }
}