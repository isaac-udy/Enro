/**
 * Enro Recipe: Per-Destination Transitions and Predictive Back
 *
 * Demonstrates overriding the transition for one destination, and customising
 * the predictive back gesture.
 *
 * Transitions normally come from the `NavigationAnimations` passed to
 * `NavigationDisplay`. A destination can override them with metadata:
 *  - `NavigationDisplay.TransitionKey` is used when the destination is opened
 *  - `NavigationDisplay.PopTransitionKey` is used when it is closed
 *  - `NavigationDisplay.PredictivePopTransitionKey` is used while the user
 *    drags a predictive back gesture away from it
 *
 * The keys are scene metadata. A scene's metadata defaults to the metadata of
 * its top destination, so setting them on a destination works with the
 * built-in single-pane scene.
 *
 * The container-level `predictivePopTransitionSpec` receives the edge the
 * swipe started from, so the gesture can animate differently for left and
 * right swipes. The gesture only exists on platforms that have one (Android,
 * iOS); elsewhere Back uses the pop transition.
 *
 * Nav3 equivalent: the Animations recipe.
 */
package dev.enro.recipes.animations

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.PredictivePopTransitionSpec
import dev.enro.ui.TransitionSpec
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object PerDestinationTransitionsRecipe : NavigationKey

@Serializable
data object TransitionsHome : NavigationKey

@Serializable
data object DefaultTransitionScreen : NavigationKey

@Serializable
data object SlideUpScreen : NavigationKey

// Opens by sliding up from the bottom.
private val slideUp: TransitionSpec = {
    ContentTransform(
        targetContentEnter = slideInVertically(tween(300)) { it },
        initialContentExit = fadeOut(tween(300)),
    )
}

// Closes by sliding back down.
private val slideDown: TransitionSpec = {
    ContentTransform(
        targetContentEnter = fadeIn(tween(300)),
        initialContentExit = slideOutVertically(tween(300)) { it },
    )
}

// During a predictive back gesture, this screen also slides down.
private val slideDownWithGesture: PredictivePopTransitionSpec = { _ ->
    ContentTransform(
        targetContentEnter = fadeIn(),
        initialContentExit = slideOutVertically { it },
    )
}

@Composable
@NavigationDestination(PerDestinationTransitionsRecipe::class)
fun PerDestinationTransitionsRecipeScreen() {
    val navigation = navigationHandle<PerDestinationTransitionsRecipe>()
    RecipeScaffold(
        title = "Per-Destination Transitions",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(TransitionsHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
            // Applies to every destination that does not override it.
            animations = NavigationAnimations.Default.copy(
                predictivePopTransitionSpec = { swipeEdge ->
                    // swipeEdge is a NavigationEvent.SwipeEdge value. A real app
                    // could slide towards the edge the swipe started from.
                    ContentTransform(
                        targetContentEnter = fadeIn(),
                        initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
                    )
                },
            ),
        )
    }
}

@Composable
@NavigationDestination(TransitionsHome::class)
fun TransitionsHomeDestination() {
    val navigation = navigationHandle<TransitionsHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Transitions", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.open(DefaultTransitionScreen) }) {
            Text("Open with the container's transition")
        }
        Button(onClick = { navigation.open(SlideUpScreen) }) {
            Text("Open with this destination's own transition")
        }
    }
}

@Composable
@NavigationDestination(DefaultTransitionScreen::class)
fun DefaultTransitionScreenDestination() {
    val navigation = navigationHandle<DefaultTransitionScreen>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Default transition", style = MaterialTheme.typography.titleLarge)
        Text("A predictive back gesture shrinks and fades this screen.")
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

@NavigationDestination(SlideUpScreen::class)
val slideUpScreenDestination: NavigationDestinationProvider<SlideUpScreen> = navigationDestination(
    metadata = {
        add(NavigationDisplay.TransitionKey.name, slideUp)
        add(NavigationDisplay.PopTransitionKey.name, slideDown)
        add(NavigationDisplay.PredictivePopTransitionKey.name, slideDownWithGesture)
    },
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Slide up", style = MaterialTheme.typography.titleLarge)
        Text("This destination sets its own open, close and predictive back transitions.")
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}
