@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import dev.enro.path.NavigationPathBinding

/**
 * Installs [module] onto the test controller managed by [runEnroTest].
 *
 * Replaces the verbose `EnroTest.getCurrentNavigationController().addModule(module)`
 * pattern that nearly every path-binding or interceptor test repeats. Must be
 * called from inside a `runEnroTest { }` block; throws if no controller is
 * installed.
 */
public fun installNavigationModule(module: NavigationModule) {
    EnroTest.getCurrentNavigationController().addModule(module)
}

/**
 * Convenience for the common case of installing a small set of
 * [NavigationPathBinding]s for a path-routing test, without manually wrapping
 * them in a `createNavigationModule { path(...) }` block.
 */
public fun installPathBindings(vararg bindings: NavigationPathBinding<*>) {
    installNavigationModule(
        createNavigationModule {
            bindings.forEach { path(it) }
        }
    )
}
