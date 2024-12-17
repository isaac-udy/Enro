package dev.enro.core.container

import androidx.activity.BackEventCompat

internal sealed class NavigationContainerBackEvent {
    data object Started : NavigationContainerBackEvent()
    data class Progressed(val backEvent: BackEventCompat) : NavigationContainerBackEvent()
    data object Cancelled : NavigationContainerBackEvent()
    data object Confirmed : NavigationContainerBackEvent()
}