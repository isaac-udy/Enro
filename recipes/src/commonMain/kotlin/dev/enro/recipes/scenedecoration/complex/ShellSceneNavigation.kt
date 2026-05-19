/**
 * Enro Recipe: Shell Scene (complex)
 *
 * A metadata-driven, multi-pane app shell that adapts to window size. Sits
 * next to the simpler `scenedecoration/simple` recipe (single sidebar /
 * bottom-bar chrome) and demonstrates how to compose Enro's scene-strategy,
 * scene-decorator, and overlay primitives into a coherent app-shell pattern.
 *
 * See `README.md` in this package for the full architecture, metadata
 * vocabulary, slot-resolution algorithm, and worked examples.
 *
 * The recipe assembles three things:
 *
 *   1. [ShellOverlaySceneStrategy] — handles `directOverlay()` destinations
 *      with breakpoint-aware drawer / sheet presentation.
 *   2. [ShellPaneSceneStrategy] — resolves the visible backstack into a
 *      `{ left?, main, right? }` triple based on `leftPane()` / `rightPane()` /
 *      `fullScreen()` metadata and the current breakpoint.
 *   3. [ShellSceneDecorator] — wraps the result in the app shell chrome
 *      (desktop top bar + left rail, or mobile top bar + bottom search/nav).
 *
 * Section roots: [ShellHome] (Home) and [ProductList] (Shop). The cart icon
 * in the top bar opens [CartOverlay].
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
