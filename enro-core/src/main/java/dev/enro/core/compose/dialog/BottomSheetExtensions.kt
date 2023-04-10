package dev.enro.core.compose.dialog

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.*
import androidx.compose.runtime.*
import dev.enro.core.AdvancedEnroApi
import dev.enro.core.compose.OverrideNavigationAnimations
import dev.enro.core.compose.navigationHandle
import dev.enro.core.parentContainer
import dev.enro.core.requestClose


@Composable
@AdvancedEnroApi
@OptIn(ExperimentalMaterialApi::class)
public fun ModalBottomSheetState.bindToNavigationHandle(): ModalBottomSheetState {
    val navigationHandle = navigationHandle()

    OverrideNavigationAnimations(
        enter = fadeIn(tween(100)),
        exit = fadeOut(tween(durationMillis = 125, delayMillis = 225))
    )

    val parent = requireNotNull(parentContainer)
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
                while(!isVisible) { runCatching { show() } }
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
    val container = requireNotNull(parentContainer)
    val isActive = remember { derivedStateOf { container.backstack.active?.instructionId == navigationHandle.id } }

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = animationSpec,
        confirmValueChange = remember(Unit) {
            fun(it: ModalBottomSheetValue): Boolean {
                val isHiding = it == ModalBottomSheetValue.Hidden
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

    content(bottomSheetState)
}