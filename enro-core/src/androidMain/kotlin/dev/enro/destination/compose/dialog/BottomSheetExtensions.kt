package dev.enro.core.compose.dialog

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.compose.OverrideNavigationAnimations
import dev.enro.core.compose.navigationHandle
import dev.enro.core.parentContainer
import dev.enro.core.requestClose
import kotlinx.coroutines.isActive


@Composable
@AdvancedEnroApi
public fun ModalBottomSheetState.bindToNavigationHandle(): ModalBottomSheetState {
    val navigationHandle = navigationHandle()

    val parent = requireNotNull(parentContainer) {
        "Failed to bind ModalBottomSheetState to NavigationHandle: parentContainer was not found"
    }
    val isInBackstack by remember {
        derivedStateOf { parent.backstack.any { it.instructionId == navigationHandle.id } }
    }
    val isActive by remember {
        derivedStateOf { parent.backstack.active?.instructionId == navigationHandle.id }
    }
    var isInitialised by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isInBackstack, isInitialised, isActive, isVisible) {
        when {
            !isInitialised -> {
                // In some cases, full screen dialogs and other things that don't necessarily render immediately
                // can cause the show animation to be cancelled, so when we're initialising, we're going to
                // force the show by looping until isVisible is true
                while(!isVisible && this@LaunchedEffect.isActive) { runCatching { show() } }
                isInitialised = true
            }
            isActive -> if(!isVisible) {
                navigationHandle.requestClose()
                if (isActive) show()
            }
            isInBackstack -> if (isVisible) hide()
            else -> hide()
        }
    }
    return this
}

@Composable
@ExperimentalMaterialApi
public fun BottomSheetDestination(
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmValueChange: (ModalBottomSheetValue) -> Boolean = { true },
    skipHalfExpanded: Boolean = false,
    content: @Composable (ModalBottomSheetState) -> Unit,
) {
    val navigationHandle = navigationHandle()
    val container = requireNotNull(parentContainer) {
        "Failed to render BottomSheetDestination: parentContainer was not found"
    }
    val isActive = remember { derivedStateOf { container.backstack.active?.instructionId == navigationHandle.id } }
    var hasBeenDisplayed by rememberSaveable { mutableStateOf(false) }

    OverrideNavigationAnimations(
        enter = fadeIn(tween(100)),
        exit = fadeOut(tween(durationMillis = 125, delayMillis = 225))
    ) {
        val bottomSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            animationSpec = animationSpec,
            confirmValueChange = remember(Unit) {
                fun(it: ModalBottomSheetValue): Boolean {
                    val isHiding = it == ModalBottomSheetValue.Hidden
                    if (isHiding && !hasBeenDisplayed) return false
                    return when {
                        !confirmValueChange(it) -> false
                        isHiding && isActive.value -> {
                            navigationHandle.requestClose()
                            !isActive.value
                        }
                        else -> true
                    }
                }
            },
            skipHalfExpanded = skipHalfExpanded,
        ).bindToNavigationHandle()

        SideEffect {
            hasBeenDisplayed = hasBeenDisplayed || bottomSheetState.isVisible
        }

        content(bottomSheetState)
    }
}