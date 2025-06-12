package dev.enro.compat

import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import dev.enro.core.NavigationDirection
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * EnroCompat provides compatibility support for applications migrating from Enro 2.x to Enro 3.x APIs.
 *
 * When this class is found on the classpath at runtime during the instantiation of Enro's Android
 * platform module, it will be automatically instantiated and its [compatModule] will be registered
 * with the navigation system.
 *
 * The compatibility module registers functionality that helps bridge the gap between the older
 * Enro 2.x APIs and the new Enro 3.x APIs, making it easier for applications to gradually migrate
 * their navigation code without breaking existing functionality.
 *
 * This includes serialization support for legacy navigation directions and other compatibility
 * features that ensure smooth interoperability between different versions of the Enro navigation
 * framework.
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