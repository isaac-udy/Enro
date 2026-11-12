@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import dev.enro.path.NavigationPathBinding

/**
 * Adds [module] to the controller installed by [runEnroTest]. Throws when no controller is
 * installed: call installNavigationModule inside a `runEnroTest { }` block.
 */
public fun installNavigationModule(module: NavigationModule) {
    EnroTest.getCurrentNavigationController().addModule(module)
}

/**
 * Adds [bindings] to the controller installed by [runEnroTest], in a new
 * `NavigationModule`. Throws when no controller is installed.
 */
public fun installPathBindings(vararg bindings: NavigationPathBinding<*>) {
    installNavigationModule(
        createNavigationModule {
            bindings.forEach { path(it) }
        }
    )
}
