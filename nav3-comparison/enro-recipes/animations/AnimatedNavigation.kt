/**
 * Enro Recipe: Animated Navigation
 *
 * Nav3 equivalent: "Animations" recipe
 * https://nicbell.github.io/nav3/recipes/animations
 *
 * Demonstrates how to customize navigation transition animations in Enro,
 * compared to Nav3's animation configuration.
 *
 * Key differences from Nav3:
 * - Nav3 configures animations on NavDisplay via transitionSpec and popTransitionSpec lambdas,
 *   which receive AnimatedContentTransitionScope and return ContentTransform.
 * - Enro uses NavigationAnimations, which provides the same transition spec lambdas but also
 *   includes predictivePopTransitionSpec and containerTransitionSpec for finer control.
 * - Both frameworks use Compose's AnimatedContent under the hood, so the animation APIs
 *   (slideInHorizontally, fadeIn, etc.) are identical.
 * - Enro's animations are set at the NavigationDisplay level (per container), not per destination.
 *   Per-destination animations can be achieved through custom NavigationSceneStrategy implementations.
 */
package dev.enro.recipes.animations

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.NavigationAnimations
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data object AnimationHome : NavigationKey

@Serializable
data class AnimatedScreen(val label: String) : NavigationKey

// -- Default Animations --
// Enro provides sensible defaults: slide + fade for forward, reverse for back.
// These match Material Design motion guidelines.

@Composable
fun DefaultAnimationsExample() {
    val container = rememberNavigationContainer(
        backstack = backstackOf(AnimationHome.asInstance()),
    )
    // NavigationAnimations.Default is used when no animations parameter is specified.
    // Default: forward = fadeIn + slideInHorizontally(1/3), back = reverse.
    NavigationDisplay(state = container)
}

// -- Custom Animations --
// Nav3 equivalent:
//   NavDisplay(backStack, transitionSpec = { ... }, popTransitionSpec = { ... })
//
// Enro equivalent:
//   NavigationDisplay(state = container, animations = NavigationAnimations(...))

@Composable
fun CustomAnimationsExample() {
    val container = rememberNavigationContainer(
        backstack = backstackOf(AnimationHome.asInstance()),
    )

    NavigationDisplay(
        state = container,
        animations = NavigationAnimations(
            // Forward navigation: slide in from right
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ) { fullWidth -> fullWidth },
                    initialContentExit = slideOutHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ) { fullWidth -> -fullWidth / 3 },
                )
            },
            // Back navigation: slide out to right
            popTransitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ) { fullWidth -> -fullWidth / 3 },
                    initialContentExit = slideOutHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ) { fullWidth -> fullWidth },
                )
            },
            // Predictive back gesture animation (Android 14+)
            // This is used during the back gesture preview, before the gesture is committed.
            predictivePopTransitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInHorizontally { fullWidth -> -fullWidth / 4 },
                    initialContentExit = slideOutHorizontally { fullWidth -> fullWidth },
                )
            },
            // Container transition: used when switching between containers (e.g., tab changes)
            containerTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(300)),
                    initialContentExit = fadeOut(tween(300)),
                )
            },
        ),
    )
}

// -- Vertical Slide Animations --
// Example: bottom-to-top slide for a modal-style flow.

@Composable
fun VerticalSlideAnimationsExample() {
    val container = rememberNavigationContainer(
        backstack = backstackOf(AnimationHome.asInstance()),
    )

    NavigationDisplay(
        state = container,
        animations = NavigationAnimations(
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInVertically { it },
                    initialContentExit = fadeOut(tween(150)),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(150)),
                    initialContentExit = slideOutVertically { it },
                )
            },
        ),
    )
}

// -- Scale + Fade Animations --
// Example: scale-based transitions for a more dramatic effect.

@Composable
fun ScaleFadeAnimationsExample() {
    val container = rememberNavigationContainer(
        backstack = backstackOf(AnimationHome.asInstance()),
    )

    NavigationDisplay(
        state = container,
        animations = NavigationAnimations(
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(300)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(300),
                    ),
                    initialContentExit = fadeOut(tween(200)) + scaleOut(
                        targetScale = 1.05f,
                        animationSpec = tween(200),
                    ),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(300)) + scaleIn(
                        initialScale = 1.05f,
                        animationSpec = tween(300),
                    ),
                    initialContentExit = fadeOut(tween(200)) + scaleOut(
                        targetScale = 0.92f,
                        animationSpec = tween(200),
                    ),
                )
            },
        ),
    )
}

// -- No Animation --
// For instant transitions (e.g., tab switches where animation is handled elsewhere).

@Composable
fun NoAnimationExample() {
    val container = rememberNavigationContainer(
        backstack = backstackOf(AnimationHome.asInstance()),
    )

    NavigationDisplay(
        state = container,
        animations = NavigationAnimations(
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(0)),
                    initialContentExit = fadeOut(tween(0)),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(0)),
                    initialContentExit = fadeOut(tween(0)),
                )
            },
        ),
    )
}

// -- Destinations --

@Composable
@NavigationDestination(AnimationHome::class)
fun AnimationHomeDestination() {
    val navigation = navigationHandle<AnimationHome>()
    Column {
        Text("Animation Home")
        Button(onClick = { navigation.open(AnimatedScreen("Screen A")) }) {
            Text("Go to Screen A")
        }
    }
}

@Composable
@NavigationDestination(AnimatedScreen::class)
fun AnimatedScreenDestination() {
    val navigation = navigationHandle<AnimatedScreen>()
    Column {
        Text("Animated Screen: ${navigation.key.label}")
        Button(onClick = { navigation.open(AnimatedScreen("Next")) }) {
            Text("Go Deeper")
        }
        Button(onClick = { navigation.close() }) {
            Text("Go Back")
        }
    }
}
