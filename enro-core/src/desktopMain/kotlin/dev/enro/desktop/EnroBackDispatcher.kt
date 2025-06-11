package dev.enro.desktop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackGestureDispatcher

@OptIn(ExperimentalComposeUiApi::class)
public class EnroBackDispatcher() : BackGestureDispatcher() {
    public fun onBack() {
        val listener = activeListener ?: return
        listener.onStarted()
        listener.onCompleted()
    }
}
