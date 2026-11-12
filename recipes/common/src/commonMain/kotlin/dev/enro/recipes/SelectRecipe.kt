@file:OptIn(ExperimentalEnroApi::class)

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
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.annotations.NavigationPath
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
import dev.enro.recipes.animations.PerDestinationTransitionsRecipe
import dev.enro.recipes.backstack.BackStackOperationsRecipe
import dev.enro.recipes.containers.NestedContainersRecipe
import dev.enro.recipes.deeplink.HandleDeepLinkRecipe
import dev.enro.recipes.dialog.NavigatingFromDialogRecipe
import dev.enro.recipes.flow.FlowInViewModelRecipe
import dev.enro.recipes.interceptor.ModuleInterceptorRecipe
import dev.enro.recipes.interop.NativeInteropRecipe
import dev.enro.recipes.koin.KoinViewModelRecipe
import dev.enro.recipes.material.MaterialListDetailRecipe
import dev.enro.recipes.material.MaterialSupportingPaneRecipe
import dev.enro.recipes.results.ViewModelResultsRecipe
import dev.enro.recipes.saveable.SavedStateRecipe
import dev.enro.recipes.viewmodel.SharedViewModelRecipe
import dev.enro.recipes.listdetail.ListDetailRecipe
import dev.enro.recipes.listdetail.ListDetailWithContainersRecipe
import dev.enro.recipes.managedflow.ManagedFlowRecipe
import dev.enro.recipes.modular.ModularNavigationRecipe
import dev.enro.recipes.multiplestacks.MultipleBackStacksRecipe
import dev.enro.recipes.plugins.OpenedTimestampPluginRecipe
import dev.enro.recipes.requestclose.RequestCloseConfirmationRecipe
import dev.enro.recipes.results.ResultsRecipe
import dev.enro.recipes.saveable.SaveableRecipe
import dev.enro.recipes.scenedecoration.complex.ShellSceneRecipe
import dev.enro.recipes.scenedecoration.simple.SceneDecorationRecipe
import dev.enro.recipes.sharedelements.SharedElementAnimationsRecipe
import dev.enro.recipes.synthetic.AuthGateSyntheticRecipe
import dev.enro.recipes.synthetic.ExternalUrlSyntheticRecipe
import dev.enro.recipes.synthetic.ProfileDeciderSyntheticRecipe
import dev.enro.recipes.tabs.TabsRecipe
import dev.enro.recipes.twopane.TwoPaneRecipe
import dev.enro.recipes.viewmodel.BasicViewModelRecipe
import dev.enro.recipes.viewmodel.SharedStateWithResultsRecipe
import kotlinx.serialization.Serializable

@Serializable
@NavigationPath("/")
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

/**
 * Recipes that only exist on the current platform, such as Fragment
 * destinations on Android. Each platform source set provides its own list.
 */
internal expect val platformRecipes: List<RecipeEntry>

private val recipeGroups: List<RecipeGroup>
    get() = commonRecipeGroups + listOfNotNull(
        RecipeGroup(title = "This Platform", recipes = platformRecipes)
            .takeIf { it.recipes.isNotEmpty() },
    )

private val commonRecipeGroups: List<RecipeGroup> = listOf(
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
                key = SavedStateRecipe,
                title = "Saved State",
                description = "Four places to keep state (remember, rememberSaveable, a " +
                    "ViewModel, SavedStateHandle) and what each one survives.",
            ),
            RecipeEntry(
                key = DestinationRegistrationRecipe,
                title = "Destination Registration",
                description = "Annotation- and provider-based destination registration.",
            ),
            RecipeEntry(
                key = BackStackOperationsRecipe,
                title = "Back Stack Operations",
                description = "Back to root, back to a specific screen, replace the current " +
                    "screen, and clear the stack.",
            ),
            RecipeEntry(
                key = BasicDeepLinkRecipe,
                title = "Basic Deep Link",
                description = "Map URL patterns to NavigationKeys with NavigationPathBinding, " +
                    "and resolve a URL to a key.",
            ),
            RecipeEntry(
                key = HandleDeepLinkRecipe,
                title = "Handling a Deep Link",
                description = "Open a destination from a URL that arrives from outside the " +
                    "app. The Android app handles enrorecipes://app/... links this way.",
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
                key = NavigatingFromDialogRecipe,
                title = "Navigating From a Dialog",
                description = "Open a screen on top of a dialog, replace a dialog with a " +
                    "screen, and stack dialogs.",
            ),
            RecipeEntry(
                key = BottomSheetRecipe,
                title = "Bottom Sheet Navigation",
                description = "ModalBottomSheet as a navigation destination.",
            ),
            RecipeEntry(
                key = ListDetailRecipe,
                title = "List-Detail (Scene)",
                description = "Adaptive list-detail using a custom NavigationSceneStrategy with " +
                    "listPane() / detailPane() metadata.",
            ),
            RecipeEntry(
                key = TwoPaneRecipe,
                title = "Two-Pane (Scene)",
                description = "Render two adjacent destinations side-by-side via a custom " +
                    "scene strategy gated by twoPane() metadata.",
            ),
            RecipeEntry(
                key = ListDetailWithContainersRecipe,
                title = "List-Detail (Containers)",
                description = "List-detail built from two NavigationContainers. Prefer the " +
                    "scene-based recipe above; this one is for when you need independent " +
                    "container filters and backstacks per pane.",
            ),
            RecipeEntry(
                key = MaterialListDetailRecipe,
                title = "Material List-Detail",
                description = "The Material 3 Adaptive ListDetailPaneScaffold as a scene " +
                    "strategy. Material decides when two panes fit.",
            ),
            RecipeEntry(
                key = MaterialSupportingPaneRecipe,
                title = "Material Supporting Pane",
                description = "The Material 3 Adaptive SupportingPaneScaffold as a scene " +
                    "strategy, driven by supportingPane() metadata.",
            ),
            RecipeEntry(
                key = NestedContainersRecipe,
                title = "Nested Containers",
                description = "Two containers side by side with accept { } filters. Shows " +
                    "which container open() sends a key to.",
            ),
            RecipeEntry(
                key = TabsRecipe,
                title = "Tab Navigation",
                description = "The minimal tab setup: one container per tab, one shown at a time.",
            ),
            RecipeEntry(
                key = MultipleBackStacksRecipe,
                title = "Multiple Back Stacks",
                description = "A backstack per tab that keeps its screens and their state " +
                    "across tab switches. Selecting the active tab again returns it to its root.",
            ),
            RecipeEntry(
                key = SceneDecorationRecipe,
                title = "Scene Decoration",
                description = "A SceneDecoratorStrategy that wraps the current scene in a " +
                    "sidebar (wide windows) or a bottom navigation bar (narrow windows) " +
                    "without re-animating the chrome when the section changes.",
            ),
            RecipeEntry(
                key = ShellSceneRecipe,
                title = "Shell Scene (multi-pane)",
                description = "A metadata-driven multi-pane app shell. Destinations tagged " +
                    "leftPane() / rightPane() / fullScreen() / directOverlay() compose into " +
                    "drawer + main + side-pane layouts that adapt to mobile / medium / wide " +
                    "window widths.",
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
                key = PerDestinationTransitionsRecipe,
                title = "Per-Destination Transitions",
                description = "Override open, close and predictive back transitions for one " +
                    "destination, and customise the predictive back gesture for a container.",
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
                key = SharedStateWithResultsRecipe,
                title = "Sharing State with Results",
                description = "Pass a value to a child in its key and get the edited value back as a result.",
            ),
            RecipeEntry(
                key = KoinViewModelRecipe,
                title = "Koin ViewModel",
                description = "A ViewModel with dependencies from Koin and arguments from " +
                    "its NavigationKey.",
            ),
            RecipeEntry(
                key = SharedViewModelRecipe,
                title = "Shared ViewModel",
                description = "One ViewModel, owned by a parent destination, used by the " +
                    "destinations in its nested container.",
            ),
            RecipeEntry(
                key = ResultsRecipe,
                title = "Returning Results",
                description = "Type-safe results with NavigationKey.WithResult.",
            ),
            RecipeEntry(
                key = ViewModelResultsRecipe,
                title = "Results in a ViewModel",
                description = "Request and receive a result from a ViewModel with " +
                    "registerForNavigationResult.",
            ),
            RecipeEntry(
                key = FlowInViewModelRecipe,
                title = "Flow in a ViewModel",
                description = "A branching multi-step flow owned by a ViewModel with " +
                    "registerForFlowResult, shown inside the host's own UI.",
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
                description = "An auth gate built with a NavigationInterceptor on the container. " +
                    "See Auth Gate under Synthetic Destinations for the other approach.",
            ),
            RecipeEntry(
                key = ModuleInterceptorRecipe,
                title = "Module Interceptor",
                description = "An interceptor registered on the NavigationComponent that " +
                    "observes opens, closes and results in every container, and blocks one open.",
            ),
            RecipeEntry(
                key = ModularNavigationRecipe,
                title = "Modular Navigation",
                description = "Three Gradle modules: shared keys and two features that open " +
                    "each other's screens without depending on each other.",
            ),
            RecipeEntry(
                key = OpenedTimestampPluginRecipe,
                title = "Opened Timestamp Plugin",
                description = "A NavigationPlugin that stamps every instance with an opened-at timestamp.",
            ),
        ),
    ),
    RecipeGroup(
        title = "Synthetic Destinations",
        recipes = listOf(
            RecipeEntry(
                key = ExternalUrlSyntheticRecipe,
                title = "External URL Launcher",
                description = "A synthetic that runs a non-Enro side effect: it opens a URL " +
                    "in the browser. No Enro state changes.",
            ),
            RecipeEntry(
                key = AuthGateSyntheticRecipe,
                title = "Auth Gate",
                description = "A synthetic that checks the auth state and forwards to either " +
                    "the requested destination or a login screen. Callers just open the " +
                    "synthetic; the gate logic lives in one place. See Conditional " +
                    "Navigation under Advanced for the interceptor approach.",
            ),
            RecipeEntry(
                key = ProfileDeciderSyntheticRecipe,
                title = "Result Decider",
                description = "A result-bearing synthetic that picks between several " +
                    "implementations at runtime (feature flag / A-B) and forwards the chosen " +
                    "one's result back to the caller via completeFrom.",
            ),
        ),
    ),
    RecipeGroup(
        title = "Interop",
        recipes = listOf(
            RecipeEntry(
                key = NativeInteropRecipe,
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
