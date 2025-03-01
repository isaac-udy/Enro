package dev.enro.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
internal fun LifecycleOwner.rememberLifecycleState() : Lifecycle.State {
    val activeState = remember(this, lifecycle.currentState) { mutableStateOf(lifecycle.currentState) }

    DisposableEffect(this, activeState) {
        val observer = LifecycleEventObserver { _, event ->
            activeState.value = event.targetState
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return activeState.value
}