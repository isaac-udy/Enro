package dev.enro.animation

public sealed interface NavigationAnimationEvent {
    public data object Entering: NavigationAnimationEvent
    public data object Exiting: NavigationAnimationEvent
    public data object ReturnEntering: NavigationAnimationEvent
    public data object ReturnExiting: NavigationAnimationEvent

//    public data class PredictiveExiting(public val progress: Float) : NavigationAnimationEvent
//    public data class PredictiveEntering(public val progress: Float) : NavigationAnimationEvent
}