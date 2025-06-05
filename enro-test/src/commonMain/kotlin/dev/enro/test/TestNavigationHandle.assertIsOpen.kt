@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package dev.enro.test

import dev.enro.core.NavigationKey

fun TestNavigationHandle<NavigationKey>.assertIsOpen() {
    assertOpenState(true)
}

fun TestNavigationHandle<NavigationKey>.assertIsNotOpen() {
    assertOpenState(false)
}

internal fun TestNavigationHandle<NavigationKey>.assertOpenState(expectedOpenState: Boolean) {
    when (expectedOpenState) {
        true -> parentContainer.assertContains(instance)
        false -> parentContainer.assertDoesNotContain(instance)
    }
}