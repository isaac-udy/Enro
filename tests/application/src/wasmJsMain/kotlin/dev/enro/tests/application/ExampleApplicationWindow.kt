package dev.enro.tests.application

import WebHistoryPlugin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.annotations.NavigationPath
import dev.enro.core.NavigationKey
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.controller.navigationController
import kotlinx.browser.window
import kotlinx.serialization.Serializable

@Serializable
//@NavigationPath("/")
object ExampleApplicationWindow : NavigationKey.SupportsPresent

@NavigationDestination(ExampleApplicationWindow::class)
@Composable
fun ExampleApplicationWindowDestination() {
    val container = rememberNavigationContainer(
        root = SelectDestination,
        emptyBehavior = EmptyBehavior.CloseParent,
    )
    DisposableEffect(Unit) {
        val historyPlugin = WebHistoryPlugin(window, container)
        window.navigationController.addPlugin(historyPlugin)
        onDispose {
            window.navigationController.removePlugin(historyPlugin)
        }
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        container.Render()
    }
}