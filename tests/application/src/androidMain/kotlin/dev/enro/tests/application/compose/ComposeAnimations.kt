package dev.enro.tests.application.compose

import android.os.Parcelable
import android.view.Window
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import dev.enro.animation.direction
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.accept
import dev.enro.core.present
import dev.enro.core.push
import dev.enro.core.requestClose
import dev.enro.destination.compose.OverrideNavigationAnimations
import dev.enro.destination.compose.navigationTransition
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object ComposeAnimations : Parcelable, NavigationKey.SupportsPush {
    @Parcelize
    internal data object Root : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal data object PushWithSlide : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal data object PushWithAnimatedSquare : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal data object Dialog : Parcelable, NavigationKey.SupportsPresent
}

private fun <T> defaultSpec() = tween<T>(1500)
private fun <T> defaultSpecDelay() = tween<T>(16, 1500)

@NavigationDestination(ComposeAnimations::class)
@Composable
fun ComposeAnimationsDestination() {
    val container = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.CloseParent,
        root = ComposeAnimations.Root,
        filter = accept {
            key { it::class.java.enclosingClass == ComposeAnimations::class.java }
        },
        animations = {
            direction(
                direction = NavigationDirection.Push,
                entering = fadeIn(defaultSpec()),
                exiting = fadeOut(defaultSpec()),
                returnEntering = fadeIn(defaultSpec()),
                returnExiting = fadeOut(defaultSpec()),
            )
        }
    )
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        container.Render()
    }
}

@NavigationDestination(ComposeAnimations.Root::class)
@Composable
fun ComposeAnimationsRoot() {
    val navigation = navigationHandle()
    TitledColumn(
        title = "Compose Animations"
    ) {
        Button(onClick = {
            navigation.push(ComposeAnimations.PushWithSlide)
        }) {
            Text(text = "Push (with slide)")
        }

        Button(onClick = {
            navigation.push(ComposeAnimations.PushWithAnimatedSquare)
        }) {
            Text(text = "Push (with animated square)")
        }


        Button(onClick = {
            navigation.present(ComposeAnimations.Dialog)
        }) {
            Text(text = "Dialog")
        }
    }
}


@OptIn(AdvancedEnroApi::class)
@NavigationDestination(ComposeAnimations.PushWithSlide::class)
@Composable
fun PushWithSlideDestination() {
    val navigationHandle = navigationHandle()
    navigationTransition.AnimatedVisibility(
        visible = { it == EnterExitState.Visible },
        enter = slideInVertically(defaultSpec()) { it / 2 },
        exit = slideOutVertically(defaultSpec()) { it / 2 },
    ) {
        TitledColumn(
            title = "Push (with slide)",
        ) {
            Button(onClick = {
                navigationHandle.requestClose()
            }) {
                Text(text = "Close")
            }
        }
    }
}

@OptIn(AdvancedEnroApi::class)
@NavigationDestination(ComposeAnimations.PushWithAnimatedSquare::class)
@Composable
fun PushWithAnimatedSquareDestination() {
    val navigationHandle = navigationHandle()
    OverrideNavigationAnimations(
        enter = fadeIn(),
        exit = fadeOut(defaultSpecDelay()),
    ) {
        TitledColumn(
            title = "Push (with animated square)",
        ) {
            Button(onClick = {
                navigationHandle.requestClose()
            }) {
                Text(text = "Close")
            }

            val size = transition.animateDp(
                label = "",
                transitionSpec = { defaultSpec() }
            ) {
                if (it == EnterExitState.Visible) 100.dp else 0.dp
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(size.value)
                    .background(Color.Red),
            )
        }
    }
}


@OptIn(AdvancedEnroApi::class)
@NavigationDestination(ComposeAnimations.Dialog::class)
@Composable
fun ComposeAnimationsDialogDestination() = DialogDestination {
    val navigationHandle = navigationHandle()
    Dialog(onDismissRequest = { navigationHandle.requestClose() }) {
        transition.AnimatedVisibility(
            visible = { it == EnterExitState.Visible },
            enter = fadeIn(defaultSpec()) + slideInVertically(defaultSpec()) { it / 2 },
            exit = fadeOut(defaultSpec()) + slideOutVertically(defaultSpec()) { it / 2 },
        ) {
            TitledColumn(
                title = "Dialog",
                modifier = Modifier.heightIn(max = 256.dp)
            ) {
                Button(onClick = {
                    navigationHandle.requestClose()
                }) {
                    Text(text = "Close")
                }
            }
        }
    }
}

@ReadOnlyComposable
@Composable
fun getDialogWindow(): Window {
    return requireNotNull((LocalView.current.parent as? DialogWindowProvider)?.window)
}
