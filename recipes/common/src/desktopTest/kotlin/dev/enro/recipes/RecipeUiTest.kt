package dev.enro.recipes

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.test.EnroTest
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

// Installs the recipes' real NavigationComponent once for the test JVM, so the
// tests run against the same destinations, interceptors and path bindings as
// the app.
private val recipesController by lazy {
    RecipesComponent.installNavigationController(Unit)
}

/**
 * Runs [block] against a recipe, hosted the way the recipes app hosts it: a
 * root container whose backstack starts with the recipe's key.
 */
@OptIn(ExperimentalTestApi::class)
internal fun runRecipeTest(
    recipe: NavigationKey,
    block: RecipeRobot.() -> Unit,
) = runComposeUiTest {
    recipesController
    // Picks up the controller installed above, and leaves it installed.
    EnroTest.installNavigationController()
    try {
        val rootContext = NavigationContextFixtures.createRootContext()
        setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                    val container = rememberNavigationContainer(
                        backstack = backstackOf(recipe.asInstance()),
                    )
                    NavigationDisplay(state = container)
                }
            }
        }
        RecipeRobot(this).block()
    } finally {
        EnroTest.uninstallNavigationController()
    }
}

/** Drives a recipe by the text on screen. Each call waits for the UI to settle. */
@OptIn(ExperimentalTestApi::class)
internal class RecipeRobot(private val test: ComposeUiTest) {
    /** Waits until [text] is on screen. */
    fun see(text: String) = waitFor("\"$text\" to be shown") {
        test.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    /** Waits until [text] is not on screen. */
    fun gone(text: String) = waitFor("\"$text\" to be removed") {
        test.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
    }

    /** Clicks the one element showing [text]. */
    fun click(text: String) {
        waitFor("exactly one \"$text\" to be shown") {
            test.onAllNodesWithText(text).fetchSemanticsNodes().size == 1
        }
        test.onNodeWithText(text).performClick()
        test.waitForIdle()
    }

    // On a timeout, fails with the text that is on screen.
    private fun waitFor(description: String, condition: () -> Boolean) {
        try {
            test.waitUntil(description, timeoutMillis = 5_000, condition = condition)
        } catch (e: ComposeTimeoutException) {
            val onScreen = test.onAllNodes(isRoot()).printToString(maxDepth = Int.MAX_VALUE)
            throw AssertionError("Timed out waiting for $description. On screen:\n$onScreen", e)
        }
    }
}
