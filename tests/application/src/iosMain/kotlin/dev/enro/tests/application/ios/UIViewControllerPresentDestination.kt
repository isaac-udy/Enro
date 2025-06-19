package dev.enro.tests.application.ios

import androidx.compose.material.Button
import androidx.compose.material.Text
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
object UIViewControllerPresentDestination : NavigationKey

@NavigationDestination(UIViewControllerPresentDestination::class)
val presentUIViewControllerDestination = uiViewControllerDestination<UIViewControllerPresentDestination>(
    metadata = {
        listOf(UIViewControllerDestination.SupportsPresent())
    },
    viewController = {
        EnroUIViewController {
            val navigation = navigationHandle()
            TitledColumn("Supports Present UIViewController") {
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