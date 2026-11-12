/**
 * Enro Recipe: Shared Element Animations
 *
 * Shows Compose shared-element transitions between Enro destinations.
 * `NavigationDisplay` renders the destinations inside one
 * `SharedTransitionLayout` and provides the scopes through
 * `LocalNavigationSharedTransitionScope` and
 * `LocalNavigationAnimatedVisibilityScope`. An element on the list screen and
 * an element on the detail screen with the same shared content key animate
 * between the two screens.
 */
@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.enro.recipes.sharedelements

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope
import dev.enro.ui.LocalNavigationSharedTransitionScope
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

private data class SharedItem(
    val id: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
)

private val items = listOf(
    SharedItem(1, "Profile", "Your account details.", Icons.Default.Person),
    SharedItem(2, "Home", "Back to the start.", Icons.Default.Home),
    SharedItem(3, "Favorites", "Things you love.", Icons.Default.Favorite),
    SharedItem(4, "Settings", "Tune the experience.", Icons.Default.Settings),
    SharedItem(5, "Mail", "Read your messages.", Icons.Default.Email),
    SharedItem(6, "Star", "Top picks.", Icons.Default.Star),
)

@Serializable
object SharedElementAnimationsRecipe : NavigationKey

@Serializable
data object SharedElementList : NavigationKey

@Serializable
data class SharedElementDetail(val id: Int) : NavigationKey

// The list screen and the detail screen must pass the same key to
// rememberSharedContentState for one shared element.
private fun iconKey(id: Int) = "shared-item-icon-$id"
private fun titleKey(id: Int) = "shared-item-title-$id"

@Composable
@NavigationDestination(SharedElementAnimationsRecipe::class)
fun SharedElementAnimationsRecipeScreen() {
    val navigation = navigationHandle<SharedElementAnimationsRecipe>()
    RecipeScaffold(
        title = "Shared Element Animations",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(SharedElementList.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(SharedElementList::class)
fun SharedElementListDestination() {
    val navigation = navigationHandle<SharedElementList>()

    // NavigationDisplay provides both scopes. Each sharedElement call in the
    // destination takes these two scopes.
    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navigation.open(SharedElementDetail(item.id)) },
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    with(sharedTransitionScope) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .sharedElement(
                                    rememberSharedContentState(key = iconKey(item.id)),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = titleKey(item.id)),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
@NavigationDestination(SharedElementDetail::class)
fun SharedElementDetailDestination() {
    val navigation = navigationHandle<SharedElementDetail>()
    val item = items.first { it.id == navigation.key.id }

    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .sharedElement(
                        rememberSharedContentState(key = iconKey(item.id)),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .size(160.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElement(
                        rememberSharedContentState(key = titleKey(item.id)),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
            )
        }

        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
