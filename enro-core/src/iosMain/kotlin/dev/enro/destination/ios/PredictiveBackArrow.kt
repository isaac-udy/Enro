package dev.enro.destination.ios

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackEventCompat
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion


@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun PredictiveBackArrow(
    enabled: Boolean,
    arrowTint: Color,
    onBack: () -> Unit,
) {
    val backEvent = remember<MutableState<BackEventCompat?>> {
        mutableStateOf(null)
    }
    PredictiveBackHandler(
        enabled = enabled,
        onBack = { progress ->
            runCatching {
                progress
                    .onCompletion { onBack() }
                    .collectLatest {
                        backEvent.value = it
                    }
            }
            backEvent.value = null
        }
    )
    PredictiveBackArrowIndicator(
        backEvent = backEvent.value,
        arrowTint = arrowTint,
    )
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun PredictiveBackArrowIndicator(
    backEvent: BackEventCompat?,
    arrowTint: Color,
) {
    val scaleX = animateFloatAsState(
        when (backEvent) {
            null -> 0f
            else -> 1f
        },
    ).value

    val startY = remember(backEvent != null) {
        backEvent?.touchY ?: 0f
    }
    val positionY = remember { mutableStateOf(0.dp) }
    if (backEvent != null) {
        positionY.value = with(LocalDensity.current) {
            lerp(startY.toDp(), backEvent.touchY.toDp(), .5f)
        }
    }
    val positionX = animateDpAsState(
        targetValue = when (backEvent) {
            null -> 0.dp
            else -> (backEvent.progress * 100).coerceAtLeast(10f).dp
        },
    )
    if (positionX.value < 1.dp) return
    Icon(
        modifier = Modifier
            .size(36.dp)
            .offset(x = (-18).dp, y = (-56).dp)
            .offset(
                y = positionY.value,
                x = positionX.value
            )
            .scale(
                scaleX = scaleX,
                scaleY = 1f,
            ),
        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
        contentDescription = null,
        tint = arrowTint,
    )
}