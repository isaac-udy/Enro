package dev.enro.extensions

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

@Composable
internal fun LifecycleOwner.rememberLifecycleState() : Lifecycle.State {
    return lifecycle.currentStateAsState().value
}