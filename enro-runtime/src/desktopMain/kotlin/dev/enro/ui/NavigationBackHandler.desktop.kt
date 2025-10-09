@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.PredictiveBackHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal actual fun NavigationBackHandler(
    enabled: Boolean,
    onBack: suspend (Flow<NavigationBackEvent>) -> Unit,
) {
    PredictiveBackHandler(
        enabled = enabled,
    ) { events ->
        onBack(
            events.map { event ->
                NavigationBackEvent(
                    touchX = event.touchX,
                    touchY = event.touchY,
                    progress = event.progress,
                    swipeEdge = event.swipeEdge,
                    frameTimeMillis = 0,
                )
            }
        )
    }
}