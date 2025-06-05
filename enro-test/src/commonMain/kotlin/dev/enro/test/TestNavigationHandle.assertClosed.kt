@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package dev.enro.test

import dev.enro.core.NavigationKey
import dev.enro.result.NavigationResult
import dev.enro.result.getResult

/**
 * Asserts that the NavigationHandle's instance has been closed
 */
fun TestNavigationHandle<NavigationKey>.assertClosed() {
    parentContainer.assertDoesNotContain { it == instance }
    val result = instance.getResult()
    require(result !is NavigationResult.Completed) {
        enroAssertionError("NavigationHandle was expected to be closed, but it was completed")
    }
}
