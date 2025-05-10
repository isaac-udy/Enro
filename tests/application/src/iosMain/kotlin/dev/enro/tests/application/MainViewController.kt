@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.tests.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import kotlinx.serialization.Serializable

@Serializable
object MainView : NavigationKey.SupportsPresent

@NavigationDestination(MainView::class)
@Composable
fun MainViewController() {
    val navigation = navigationHandle()
    val container = rememberNavigationContainer(
        root = SelectDestination,
        emptyBehavior = EmptyBehavior.CloseParent,
    )
    Column(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        IconButton(
            onClick = {
                container.context.leafContext().navigationHandle.requestClose()
            }
        ) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, null)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            container.Render()
        }
    }
}
