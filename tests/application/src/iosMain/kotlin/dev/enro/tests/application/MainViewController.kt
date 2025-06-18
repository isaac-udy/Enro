package dev.enro.tests.application

import dev.enro.asInstance
import dev.enro.platform.EnroUIViewController
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = EnroUIViewController {
    val container = rememberNavigationContainer(
        backstack = listOf(SelectDestination().asInstance())
    )
    NavigationDisplay(
        state = container,
    )
}
