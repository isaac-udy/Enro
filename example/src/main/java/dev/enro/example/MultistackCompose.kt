package dev.enro.example

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import dev.enro.annotations.ExperimentalComposableDestination
import dev.enro.annotations.NavigationDestination
import dev.enro.core.EmptyBehavior
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.*
import kotlinx.parcelize.Parcelize

@Parcelize
class MultistackComposeKey : NavigationKey

@OptIn(ExperimentalAnimationApi::class)
@Composable
@ExperimentalComposableDestination
@NavigationDestination(MultistackComposeKey::class)
fun MultistackComposeScreen() {

//    val composableManager = localComposableManager
    val redController = rememberEnroContainerController(
        initialBackstack = listOf(NavigationInstruction.Forward(ComposeSimpleExampleKey("Red", "Mutlistack"))),
        emptyBehavior = EmptyBehavior.CloseParent
    )

    val greenController = rememberEnroContainerController(
        initialBackstack = listOf(NavigationInstruction.Forward(ComposeSimpleExampleKey("Green", "Mutlistack"))),
        emptyBehavior = EmptyBehavior.Action {
//            composableManager.setActiveContainer(redController)
            true
        }
    )

    val blueController = rememberEnroContainerController(
        initialBackstack = listOf(NavigationInstruction.Forward(ComposeSimpleExampleKey("Blue", "Mutlistack"))),
        emptyBehavior = EmptyBehavior.Action {
//            composableManager.setActiveContainer(redController)
            true
        }
    )

//    Column {
//        Crossfade(
//            targetState = composableManager.activeContainer,
//            modifier = Modifier.weight(1f, true),
//            animationSpec = tween(225)
//        ) {
//            if(it == null) return@Crossfade
//            val isActive = composableManager.activeContainer == it
//            EnroContainer(
//                controller = it,
//                modifier = Modifier
//                    .weight(1f)
//                    .animateVisibilityWithScale(
//                        visible = isActive,
//                        enterScale = 0.9f,
//                        exitScale = 1.1f,
//                    )
//                    .zIndex(if (isActive) 1f else 0f)
//            )
//        }
//        BottomAppBar(
//            backgroundColor = Color.White
//        ) {
//            TextButton(onClick = {
//                composableManager.setActiveContainer(redController)
//            }) {
//                Text(text = "Red")
//            }
//            TextButton(onClick = {
//                composableManager.setActiveContainer(greenController)
//            }) {
//                Text(text = "Green")
//            }
//            TextButton(onClick = {
//                composableManager.setActiveContainer(blueController)
//            }) {
//                Text(text = "Blue")
//            }
//        }
//    }
}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.animateVisibilityWithScale(
    visible: Boolean,
    enterScale: Float,
    exitScale: Float
): Modifier = composed {
    val isFirstRender = remember { mutableStateOf(true) }
    val anim = animateFloatAsState(
        targetValue = when {
            isFirstRender.value -> enterScale
            visible -> 1.0f
            else -> exitScale
        },
        animationSpec = tween(225)
    )
    SideEffect {
        isFirstRender.value = false
    }

    return@composed scale(anim.value)
}