package dev.enro.tests.application.ios

import androidx.compose.material.Text
import androidx.compose.ui.window.ComposeUIViewController
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.destinations.UIViewControllerDestination
import dev.enro.ui.destinations.uiViewControllerDestination
import kotlinx.serialization.Serializable

@Serializable
object UIViewControllerComposeDestination : NavigationKey

@NavigationDestination(UIViewControllerComposeDestination::class)
val composeUIViewControllerDestination = uiViewControllerDestination<UIViewControllerComposeDestination>(
    metadata = {
        listOf(UIViewControllerDestination.SupportsCompose)
    },
    viewController = {
        ComposeUIViewController {
            val navigation = navigationHandle()
            TitledColumn("Supports Compose UIViewController") {
                Text("This is a ComposeUIViewController")
            }
        }
    }
)