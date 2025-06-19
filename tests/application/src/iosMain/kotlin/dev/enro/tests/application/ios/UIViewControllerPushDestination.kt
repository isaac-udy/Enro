package dev.enro.tests.application.ios

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.platform.EnroUIViewController
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.destinations.UIViewControllerDestination
import dev.enro.ui.destinations.uiViewControllerDestination
import kotlinx.serialization.Serializable

@Serializable
object UIViewControllerPushDestination : NavigationKey

@NavigationDestination(UIViewControllerPushDestination::class)
val pushedUIViewControllerDestination = uiViewControllerDestination<UIViewControllerPushDestination>(
    metadata = {
        listOf(UIViewControllerDestination.SupportsUINavigationView)
    },
    viewController = {
        EnroUIViewController {
            val navigation = navigationHandle()
            TitledColumn(
                title = "Supports Push in UINavigationView",
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                Button(
                    onClick = {
                        navigation.close()
                    }
                ) {
                    Text("Close")
                }
            }
        }
    }
)