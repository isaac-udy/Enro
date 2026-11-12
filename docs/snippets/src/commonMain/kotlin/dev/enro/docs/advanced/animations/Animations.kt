// Snippets for docs/ghpages/docs/advanced/animations.md
@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.enro.docs.advanced.animations

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope
import dev.enro.ui.LocalNavigationSharedTransitionScope
import dev.enro.ui.NavigationAnimations
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.animation.NavigationAnimatedVisibility
import dev.enro.ui.animation.animateNavigationEnterExit
import dev.enro.ui.contains
import dev.enro.ui.decorators.navigationDestinationDecorator
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.isDirectOverlay
import kotlinx.serialization.Serializable

@Serializable
data object MyScreen : NavigationKey

@Serializable
data object ItemList : NavigationKey

@Serializable
data class ItemDetail(val id: Int) : NavigationKey

data class Item(val id: Int)

private val myItems = listOf(Item(1), Item(2), Item(3))

// region docs:animations/background-per-destination
@Composable
@NavigationDestination(MyScreen::class)
fun MyScreenDestination() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        // ...
    }
}
// endregion

// region docs:animations/background-decorator
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        decorator {
            navigationDestinationDecorator { destination ->
                val isOverlay = destination.isDirectOverlay() ||
                    destination.metadata.contains(DialogSceneStrategy.DialogPropertiesKey)
                if (isOverlay) {
                    destination.Content()
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        destination.Content()
                    }
                }
            }
        }
    }
)
// endregion

@Composable
fun NavigationAnimationsExample(container: NavigationContainerState) {
    // region docs:animations/navigation-animations
    NavigationDisplay(
        state = container,
        animations = NavigationAnimations(
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInHorizontally { it } + fadeIn(tween(200)),
                    initialContentExit = slideOutHorizontally { -it / 3 } + fadeOut(tween(150)),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInHorizontally { -it / 3 } + fadeIn(tween(200)),
                    initialContentExit = slideOutHorizontally { it } + fadeOut(tween(150)),
                )
            },
        ),
    )
    // endregion
}

@Composable
fun EnterExitExample() {
    Box {
        // region docs:animations/enter-exit-modifier
        Box(
            Modifier
                .matchParentSize()
                .animateNavigationEnterExit(
                    enter = fadeIn(tween(durationMillis = 320)),
                    exit = fadeOut(tween(durationMillis = 220)),
                )
                .background(Color.Black.copy(alpha = 0.5f)),
        )
        // endregion
    }
}

@Composable
fun AnimatedVisibilityExample() {
    // region docs:animations/animated-visibility
    NavigationAnimatedVisibility(
        enter = fadeIn(tween(durationMillis = 220, delayMillis = 140)),
        exit = fadeOut(tween(durationMillis = 100)),
    ) {
        Row { /* action buttons that come in slightly later than the rest */ }
    }
    // endregion
}

@Composable
fun PredictiveBackExample(container: NavigationContainerState) {
    // region docs:animations/predictive-back
    NavigationDisplay(
        state = container,
        animations = NavigationAnimations.Default.copy(
            predictivePopTransitionSpec = { swipeEdge ->
                ContentTransform(
                    targetContentEnter = fadeIn(),
                    initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
                )
            },
        ),
    )
    // endregion
}

// region docs:animations/shared-elements
@Composable
@NavigationDestination(ItemList::class)
fun ItemListDestination() {
    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current

    LazyColumn {
        items(myItems) { item ->
            with(sharedTransitionScope) {
                // The item's thumbnail.
                Box(
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState(key = "thumb-${item.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .size(48.dp),
                )
            }
        }
    }
}

@Composable
@NavigationDestination(ItemDetail::class)
fun ItemDetailDestination() {
    val navigation = navigationHandle<ItemDetail>()
    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current

    with(sharedTransitionScope) {
        // The same thumbnail, larger.
        Box(
            modifier = Modifier
                .sharedElement(
                    rememberSharedContentState(key = "thumb-${navigation.key.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .size(160.dp),
        )
    }
}
// endregion

// region docs:animations/shared-element-keys
private fun thumbKey(id: Int) = "thumb-$id"
private fun titleKey(id: Int) = "title-$id"
// endregion
