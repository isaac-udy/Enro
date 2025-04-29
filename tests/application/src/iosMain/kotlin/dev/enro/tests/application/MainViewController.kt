@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.tests.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import dev.enro.destination.uiviewcontroller.EnroComposeUIViewController
import kotlinx.serialization.Serializable
import platform.UIKit.UIViewController

@Serializable
object MainView : NavigationKey.SupportsPresent



@NavigationDestination(MainView::class)
fun MainViewController(): UIViewController {
    return EnroComposeUIViewController {
        val navigation = navigationHandle()
        val container = rememberNavigationContainer(
            root = SelectDestination,
            emptyBehavior = EmptyBehavior.CloseParent,
        )

        Column {
            Spacer(Modifier.height(100.dp))
            IconButton(
                onClick = {
                    container.context.leafContext().navigationHandle.requestClose()
                }
            ) {
                Icon(Icons.Default.ArrowBack, null)
            }
            Box(
                modifier = Modifier
                    .background(Color.LightGray)
                    .fillMaxSize()
            ) {
                container.Render()
            }
        }
    }
}
