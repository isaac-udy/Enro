/**
 * Enro Recipe: Animated Navigation
 *
 * Demonstrates customising navigation transition animations with NavigationAnimations.
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationAnimations
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object AnimationsRecipe : NavigationKey

@Serializable
data object AnimationHome : NavigationKey

@Serializable
data class AnimatedScreen(val label: String) : NavigationKey

private enum class AnimationStyle(val displayName: String) {
    Default("Default"),
    SlideHorizontal("Slide H"),
    SlideVertical("Slide V"),
    ScaleFade("Scale + Fade"),
    None("None"),
}

@Composable
@NavigationDestination(AnimationsRecipe::class)
fun AnimationsRecipeScreen() {
    val navigation = navigationHandle<AnimationsRecipe>()
    var style by rememberSaveable { mutableStateOf(AnimationStyle.Default) }

    RecipeScaffold(
        title = "Animated Navigation",
        navigation = navigation,
    ) { modifier ->
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Animation: ${style.displayName}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AnimationStyle.entries.forEach { entry ->
                    Button(onClick = { style = entry }) {
                        Text(entry.displayName)
                    }
                }
            }

            val animations = when (style) {
                AnimationStyle.Default -> NavigationAnimations.Default
                AnimationStyle.SlideHorizontal -> customSlideAnimations()
                AnimationStyle.SlideVertical -> verticalSlideAnimations()
                AnimationStyle.ScaleFade -> scaleFadeAnimations()
                AnimationStyle.None -> noAnimations()
            }

            val container = rememberNavigationContainer(
                backstack = backstackOf(AnimationHome.asInstance()),
            )
            NavigationDisplay(
                state = container,
                animations = animations,
                modifier = Modifier.fillMaxSize().padding(8.dp),
            )
        }
    }
}

private fun customSlideAnimations(): NavigationAnimations {
    return NavigationAnimations(
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
    )
}

private fun verticalSlideAnimations(): NavigationAnimations {
    return NavigationAnimations(
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
    )
}

private fun scaleFadeAnimations(): NavigationAnimations {
    return NavigationAnimations(
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
    )
}

private fun noAnimations(): NavigationAnimations {
    return NavigationAnimations(
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
    )
}

@Composable
@NavigationDestination(AnimationHome::class)
fun AnimationHomeDestination() {
    val navigation = navigationHandle<AnimationHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Animated Screen: ${navigation.key.label}")
        Button(onClick = { navigation.open(AnimatedScreen("Next")) }) {
            Text("Go Deeper")
        }
        Button(onClick = { navigation.close() }) {
            Text("Go Back")
        }
    }
}
