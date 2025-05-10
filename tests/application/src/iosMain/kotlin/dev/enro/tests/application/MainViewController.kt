@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.tests.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.getNavigationHandle
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import dev.enro.destination.compose.navigationContext
import dev.enro.destination.ios.PredictiveBackArrow
import kotlinx.serialization.Serializable

@Serializable
object MainView : NavigationKey.SupportsPresent

@NavigationDestination(MainView::class)
@Composable
fun MainViewController() {
    val navigation = navigationHandle()
    val navigationContext = navigationContext
    val container = rememberNavigationContainer(
        root = SelectDestination,
        emptyBehavior = EmptyBehavior.CloseParent,
    )
    Column(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            container.Render()
            PredictiveBackArrow(
                enabled = container.backstack.size > 1,
                arrowTint = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                onBack = { navigationContext.leafContext().getNavigationHandle().requestClose() }
            )
        }
    }
}
