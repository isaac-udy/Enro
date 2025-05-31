package dev.enro3.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
internal actual fun NavigationBackHandler(
    enabled: Boolean,
    onBack: suspend (Flow<NavigationBackEvent>) -> Unit
) {
    @SuppressLint("NoCollectCallFound")
    PredictiveBackHandler(
        enabled = enabled,
    ) { events ->
        onBack(
            events.map { backEvent ->
                NavigationBackEvent(
                    touchX = backEvent.touchX,
                    touchY = backEvent.touchY,
                    progress = backEvent.progress,
                    swipeEdge = backEvent.swipeEdge,
                    frameTimeMillis = when {
                        Build.VERSION.SDK_INT >= 36 -> 0//backEvent.frameTimeMillis
                        else -> 0
                    },
                )
            }
        )
    }
}