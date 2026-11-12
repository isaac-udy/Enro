/**
 * Enro Recipe: Shell Scene (complex)
 *
 * Shows a multi-pane app shell that adapts to window size, built from two
 * `NavigationSceneStrategy` classes, a `SceneDecoratorStrategy` and
 * destination metadata. The `scenedecoration/simple` recipe shows a
 * `SceneDecoratorStrategy` alone.
 *
 * `README.md` in this package documents the metadata, the slot resolution
 * and worked examples.
 *
 * The recipe passes three classes to `NavigationDisplay`:
 *
 *   1. [ShellOverlaySceneStrategy] renders a `directOverlay()` destination
 *      as a drawer or a bottom sheet, by breakpoint.
 *   2. [ShellPaneSceneStrategy] assigns the top backstack entries to the
 *      slots `{ left?, main, right? }` from the `leftPane()`, `rightPane()`
 *      and `fullScreen()` metadata and the breakpoint.
 *   3. [ShellSceneDecorator] wraps the scene in the shell chrome: a top bar
 *      and left rail, or on Mobile a top bar and a bottom search and
 *      navigation row.
 *
 * The section roots are [ShellHome] (Home) and [ProductList] (Shop). The cart
 * icon in the top bar opens `CartOverlay`.
 */
package dev.enro.recipes.scenedecoration.complex

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.recipes.RecipeScaffold
import dev.enro.recipes.scenedecoration.complex.destinations.ProductDetail
import dev.enro.recipes.scenedecoration.complex.destinations.ProductList
import dev.enro.recipes.scenedecoration.complex.destinations.ShellHome
import dev.enro.ui.NavigationAnimations
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import kotlinx.serialization.Serializable

@Serializable
object ShellSceneRecipe : NavigationKey

@Composable
@NavigationDestination(ShellSceneRecipe::class)
fun ShellSceneRecipeScreen() {
    val navigation = navigationHandle<ShellSceneRecipe>()
    RecipeScaffold(
        title = "Shell Scene",
        topBar = {},
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(ShellHome.asInstance()),
            // The interceptor replaces the open of a ProductDetail with an
            // AggregateOperation that also closes each ProductDetail on top
            // of the backstack, so the backstack ends with one ProductDetail.
            interceptor = navigationInterceptor {
                onOpened<ProductDetail> {
                    val existingProductDetails = backstack.takeLastWhile { it.key is ProductDetail }
                    if (existingProductDetails.isEmpty()) continueWithOpen()
                    replaceWith(
                        NavigationOperation.AggregateOperation(
                            instance.asOpenOperation(),
                            *existingProductDetails.map { it.asCloseOperation() }.toTypedArray(),
                        )
                    )
                }
            }
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
            sceneStrategy = remember {
                NavigationSceneStrategy.from(
                    ShellOverlaySceneStrategy(),
                    ShellPaneSceneStrategy(),
                    SinglePaneSceneStrategy(),
                )
            },
            animations = NavigationAnimations.Default.copy(
                transitionSpec = NavigationAnimations.Default.containerTransitionSpec,
                popTransitionSpec = NavigationAnimations.Default.containerTransitionSpec,
            ),
            sceneDecoratorStrategies = listOf(
                remember {
                    ShellSceneDecorator(
                        sections = listOf(
                            ShellSection(ShellHome, "Home", Icons.Filled.Home),
                            ShellSection(ProductList, "Shop", Icons.Filled.Search),
                        ),
                        onClose = {
                            if (container.backstack.size == 1) {
                                navigation.close()
                            } else {
                                container.updateBackstack { it.dropLast(1).asBackstack() }
                            }
                        },
                    )
                },
            ),
        )
    }
}
