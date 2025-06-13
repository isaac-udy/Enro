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
    val transitionSpec: AnimatedContentTransitionScope<out SceneTransitionData>.() -> ContentTransform = {
        ContentTransform(
            targetContentEnter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + slideInHorizontally { it / 3 },
            initialContentExit = slideOutHorizontally { -it / 4 },
        )
    },
    val popTransitionSpec: AnimatedContentTransitionScope<out SceneTransitionData>.() -> ContentTransform = {
        ContentTransform(
            targetContentEnter = slideInHorizontally { -it / 4 },
            initialContentExit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutHorizontally { it / 3 },
        )
    },
    val predictivePopTransitionSpec: AnimatedContentTransitionScope<out SceneTransitionData>.() -> ContentTransform = popTransitionSpec,
    val containerTransitionSpec: AnimatedContentTransitionScope<out SceneTransitionData>.() -> ContentTransform = {
        ContentTransform(
            targetContentEnter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
            initialContentExit = fadeOut(),
        )
    },
    // If this is set to true, transitions to-and-from an empty backstack will use the container transform,
    // instead of the normal transitionSpec/popTransitionSpec.
    val emptyUsesContainerTransition: Boolean,
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
            predictivePopTransitionSpec = {
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