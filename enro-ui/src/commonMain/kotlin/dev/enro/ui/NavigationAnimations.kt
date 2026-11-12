package dev.enro.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

public data class NavigationAnimations(
    val transitionSpec: TransitionSpec = {
        ContentTransform(
            targetContentEnter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + slideInHorizontally { it / 3 },
            initialContentExit = slideOutHorizontally { -it / 4 },
        )
    },
    val popTransitionSpec: TransitionSpec = {
        ContentTransform(
            targetContentEnter = slideInHorizontally { -it / 4 },
            initialContentExit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutHorizontally { it / 3 },
        )
    },
    /**
     * The spec receives the `NavigationEvent.SwipeEdge` of the back gesture.
     */
    val predictivePopTransitionSpec: PredictivePopTransitionSpec = { _ ->
        popTransitionSpec()
    },
    val containerTransitionSpec: TransitionSpec = {
        ContentTransform(
            targetContentEnter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
            initialContentExit = fadeOut(),
        )
    },
    // When true, a transition to or from an empty backstack uses containerTransitionSpec
    // instead of transitionSpec or popTransitionSpec.
    val emptyUsesContainerTransition: Boolean = true,
) {
    public companion object {
        public val Default: NavigationAnimations = NavigationAnimations(
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + slideInHorizontally { it / 3 },
                    initialContentExit = slideOutHorizontally { -it / 4 },
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInHorizontally { -it / 4 },
                    initialContentExit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutHorizontally { it / 3 },
                )
            },
            predictivePopTransitionSpec = { _ ->
                ContentTransform(
                    targetContentEnter = slideInHorizontally { -it / 4 },
                    initialContentExit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutHorizontally { it / 3 },
                )
            },
            containerTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(),
                    initialContentExit = fadeOut(),
                )
            },
            emptyUsesContainerTransition = true,
        )
    }
}