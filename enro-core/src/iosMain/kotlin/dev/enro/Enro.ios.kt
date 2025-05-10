package dev.enro

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.destination.ios.EnroUIViewController
import dev.enro.destination.ios.createUIViewControllerNavigationBinding
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import platform.UIKit.UIViewController
import kotlin.reflect.KClass

/**
 * This class is used as an entry point for Enro on iOS. In Swift sources that are using Enro,
 * there are some issues with the visibility of various classes and functions defined in
 * the enro-core module. This class is used to expose the Enro API in a way that is more
 * compatible with Swift.
 */
@ObjCName("Enro", exact = true)
public object Enro {

    public val backConfiguration: BackConfiguration = BackConfiguration()

    public class BackConfiguration internal constructor() {
        public val Default: EnroBackConfiguration = EnroBackConfiguration.Default
        public val Predictive: EnroBackConfiguration = EnroBackConfiguration.Predictive
        public val Manual: EnroBackConfiguration = EnroBackConfiguration.Manual
    }

    @OptIn(InternalSerializationApi::class, BetaInteropApi::class)
    public fun addUIViewControllerNavigationBinding(
        scope: NavigationModuleScope,
        keyType: ObjCClass,
        constructDestination: () -> UIViewController,
    ) {
        val originalClass = getOriginalKotlinClass(keyType)
        requireNotNull(originalClass) {
            "Could not find a Kotlin class for $keyType."
        }
        @Suppress("UNCHECKED_CAST")
        scope.binding(
            createUIViewControllerNavigationBinding<NavigationKey, UIViewController>(
                keyType = originalClass as KClass<NavigationKey>,
                keySerializer = originalClass.serializer(),
                destinationType = UIViewController::class,
                constructDestination = constructDestination,
            )
        )
    }

    public fun createEnroViewController(
        present: NavigationInstruction.Open<NavigationDirection.Present>,
    ): UIViewController {
        return EnroUIViewController(present)
    }

    public fun createEnroViewController(
        push: NavigationInstruction.Open<NavigationDirection.Push>,
    ): UIViewController {
        return EnroUIViewController(push)
    }

    public fun createEnroViewController(
        present: NavigationInstruction.Open<NavigationDirection.Present>,
        controller: () -> UIViewController,
    ): UIViewController {
        return EnroUIViewController(present, controller)
    }

    public fun createEnroViewController(
        push: NavigationInstruction.Open<NavigationDirection.Push>,
        controller: () -> UIViewController,
    ): UIViewController {
        return EnroUIViewController(push, controller)
    }
}