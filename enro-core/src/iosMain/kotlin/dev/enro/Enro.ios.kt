package dev.enro

import dev.enro.core.NavigationKey
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.NavigationModuleScope
import platform.UIKit.UIViewController

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

    public fun <KeyType : NavigationKey> addUIViewControllerNavigationBinding(
        scope: NavigationModuleScope,
        key: KeyType,
        constructDestination: () -> UIViewController,
    ) {
        scope.binding(
            dev.enro.destination.uiviewcontroller.createUIViewControllerNavigationBinding(
                keyType = key::class,
                destinationType = UIViewController::class,
                constructDestination = constructDestination,
            )
        )
    }
}