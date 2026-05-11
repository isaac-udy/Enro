package dev.enro.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.animations.AnimationsRecipe
import dev.enro.recipes.animations.StaggeredAnimationsRecipe
import dev.enro.recipes.basic.BasicRecipe
import dev.enro.recipes.bottomsheet.BottomSheetRecipe
import dev.enro.recipes.conditional.ConditionalRecipe
import dev.enro.recipes.deeplink.AdvancedDeepLinkRecipe
import dev.enro.recipes.deeplink.BasicDeepLinkRecipe
import dev.enro.recipes.dialog.DialogRecipe
import dev.enro.recipes.entryprovider.DestinationRegistrationRecipe
import dev.enro.recipes.interop.FragmentInteropRecipe
import dev.enro.recipes.listdetail.ListDetailRecipe
import dev.enro.recipes.managedflow.ManagedFlowRecipe
import dev.enro.recipes.modular.ModularNavigationRecipe
import dev.enro.recipes.multiplestacks.MultipleBackStacksRecipe
import dev.enro.recipes.plugins.OpenedTimestampPluginRecipe
import dev.enro.recipes.requestclose.RequestCloseConfirmationRecipe
import dev.enro.recipes.results.ResultsRecipe
import dev.enro.recipes.saveable.SaveableRecipe
import dev.enro.recipes.sharedelements.SharedElementAnimationsRecipe
import dev.enro.recipes.tabs.TabsRecipe
import dev.enro.recipes.viewmodel.BasicViewModelRecipe
import dev.enro.recipes.viewmodel.SharedViewModelRecipe
import kotlinx.serialization.Serializable

@Serializable
object SelectRecipe : NavigationKey

internal data class RecipeEntry(
    val key: NavigationKey,
    val title: String,
    val description: String,
)

internal data class RecipeGroup(
    val title: String,
    val recipes: List<RecipeEntry>,
)

private val recipeGroups: List<RecipeGroup> = listOf(
    RecipeGroup(
        title = "Basic",
        recipes = listOf(
            RecipeEntry(
                key = BasicRecipe,
                title = "Basic Navigation",
                description = "Forward and back navigation between screens.",
            ),
            RecipeEntry(
                key = SaveableRecipe,
                title = "Saveable Back Stack",
                description = "Backstack and rememberSaveable state survives configuration changes.",
            ),
            RecipeEntry(
                key = DestinationRegistrationRecipe,
                title = "Destination Registration",
                description = "Annotation- and provider-based destination registration.",
            ),
            RecipeEntry(
                key = BasicDeepLinkRecipe,
                title = "Basic Deep Link",
                description = "Map URL patterns to NavigationKeys with NavigationPathBinding.",
            ),
            RecipeEntry(
                key = AdvancedDeepLinkRecipe,
                title = "Advanced Deep Link",
                description = "Synthetic backstacks for deep links.",
            ),
        ),
    ),
    RecipeGroup(
        title = "Scenes & Overlays",
        recipes = listOf(
            RecipeEntry(
                key = DialogRecipe,
                title = "Dialog Navigation",
                description = "Dialog destinations using dialog() and directOverlay() metadata.",
            ),
            RecipeEntry(
                key = BottomSheetRecipe,
                title = "Bottom Sheet Navigation",
                description = "ModalBottomSheet as a navigation destination.",
            ),
            RecipeEntry(
                key = ListDetailRecipe,
                title = "List-Detail Navigation",
                description = "Adaptive single/dual-pane list-detail layouts.",
            ),
            RecipeEntry(
                key = TabsRecipe,
                title = "Tab Navigation",
                description = "Multiple containers with NavigationContainerGroup.",
            ),
            RecipeEntry(
                key = MultipleBackStacksRecipe,
                title = "Multiple Back Stacks",
                description = "Independent backstacks per tab using NavigationContainerGroup.",
            ),
        ),
    ),
    RecipeGroup(
        title = "Animations",
        recipes = listOf(
            RecipeEntry(
                key = AnimationsRecipe,
                title = "Animated Navigation",
                description = "Customise transitions with NavigationAnimations.",
            ),
            RecipeEntry(
                key = StaggeredAnimationsRecipe,
                title = "Staggered Animations",
                description = "Animate parts of a destination on their own timing with " +
                    "Modifier.animateNavigationEnterExit and NavigationAnimatedVisibility.",
            ),
            RecipeEntry(
                key = SharedElementAnimationsRecipe,
                title = "Shared Element Animations",
                description = "Compose's sharedElement transitions across destinations.",
            ),
        ),
    ),
    RecipeGroup(
        title = "State & Results",
        recipes = listOf(
            RecipeEntry(
                key = BasicViewModelRecipe,
                title = "Basic ViewModel",
                description = "Use NavigationHandle inside a ViewModel.",
            ),
            RecipeEntry(
                key = SharedViewModelRecipe,
                title = "Shared State",
                description = "Share state between destinations.",
            ),
            RecipeEntry(
                key = ResultsRecipe,
                title = "Returning Results",
                description = "Type-safe results with NavigationKey.WithResult.",
            ),
            RecipeEntry(
                key = ManagedFlowRecipe,
                title = "Managed Flow",
                description = "Multi-step flows defined as sequential code.",
            ),
            RecipeEntry(
                key = RequestCloseConfirmationRecipe,
                title = "Request-Close Confirmation",
                description = "Override onCloseRequested to confirm discarding unsaved changes.",
            ),
        ),
    ),
    RecipeGroup(
        title = "Advanced",
        recipes = listOf(
            RecipeEntry(
                key = ConditionalRecipe,
                title = "Conditional Navigation",
                description = "Auth gates with NavigationInterceptors.",
            ),
            RecipeEntry(
                key = ModularNavigationRecipe,
                title = "Modular Navigation",
                description = "Destinations across feature modules with KSP.",
            ),
            RecipeEntry(
                key = OpenedTimestampPluginRecipe,
                title = "Opened Timestamp Plugin",
                description = "A NavigationPlugin that stamps every instance with an opened-at timestamp.",
            ),
        ),
    ),
    RecipeGroup(
        title = "Interop",
        recipes = listOf(
            RecipeEntry(
                key = FragmentInteropRecipe,
                title = "Native Interop",
                description = "Embed native UI (AndroidView / SwingPanel / UIKitView) inside a destination.",
            ),
        ),
    ),
)

@Composable
@NavigationDestination(SelectRecipe::class)
fun SelectRecipeScreen() {
    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enro Recipes",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Demonstrations of common navigation patterns in Enro.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
        }

        recipeGroups.forEach { group ->
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            group.recipes.forEach { recipe ->
                item {
                    RecipeCard(recipe = recipe)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RecipeCard(recipe: RecipeEntry) {
    val navigation = navigationHandle()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f),
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            TextButton(
                modifier = Modifier.widthIn(min = 56.dp),
                onClick = { navigation.open(recipe.key) },
            ) {
                Text("Open")
            }
        }
    }
}
