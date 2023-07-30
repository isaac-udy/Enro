package dev.enro.test.application

import androidx.compose.ui.test.junit4.ComposeTestRule
import dev.enro.core.NavigationHandle
import dev.enro.tests.application.TestApplicationPlugin

fun ComposeTestRule.waitForNavigationHandle(
    block: (NavigationHandle) -> Boolean
): NavigationHandle {
    var navigationHandle: NavigationHandle? = null
    waitUntil {
        navigationHandle = TestApplicationPlugin.activeNavigationHandle
        navigationHandle != null && block(navigationHandle!!)
    }
    return navigationHandle!!
}
