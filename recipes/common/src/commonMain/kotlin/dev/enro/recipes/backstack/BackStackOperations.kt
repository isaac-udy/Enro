/**
 * Enro Recipe: Back Stack Operations
 *
 * Shows four backstack changes other than open and close:
 *  - back to the root of the container
 *  - back to a screen that is already on the backstack ("pop up to")
 *  - clear the backstack and start again with a different screen
 *  - replace the current screen with another screen
 *
 * The first three changes call
 * `LocalNavigationContainer.current.updateBackstack { ... }`. The block receives
 * the current backstack and returns the new backstack. The fourth change calls
 * `navigation.closeAndReplaceWith(key)`.
 */
package dev.enro.recipes.backstack

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
import dev.enro.asBackstack
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.closeAndReplaceWith
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object BackStackOperationsRecipe : NavigationKey

@Serializable
data class StackStep(val number: Int) : NavigationKey

@Serializable
data object StackFinished : NavigationKey

@Composable
@NavigationDestination(BackStackOperationsRecipe::class)
fun BackStackOperationsRecipeScreen() {
    val navigation = navigationHandle<BackStackOperationsRecipe>()
    RecipeScaffold(
        title = "Back Stack Operations",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(StackStep(1).asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(StackStep::class)
fun StackStepDestination() {
    val navigation = navigationHandle<StackStep>()
    // LocalNavigationContainer is the container that displays this destination.
    val container = LocalNavigationContainer.current
    val step = navigation.key.number

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Step $step", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Stack: " + container.backstack.joinToString(" > ") { instance ->
                when (val key = instance.key) {
                    is StackStep -> "Step ${key.number}"
                    else -> "Finished"
                }
            },
            style = MaterialTheme.typography.bodySmall,
        )

        Button(onClick = { navigation.open(StackStep(step + 1)) }) {
            Text("Open step ${step + 1}")
        }

        // Back to root: the new backstack contains only the first entry.
        Button(onClick = { container.updateBackstack { it.take(1).asBackstack() } }) {
            Text("Back to root")
        }

        // Pop up to: the new backstack ends at Step 2. The block returns the
        // backstack unchanged when Step 2 is not on the backstack.
        Button(onClick = {
            container.updateBackstack { backstack ->
                val index = backstack.indexOfLast { (it.key as? StackStep)?.number == 2 }
                if (index == -1) backstack else backstack.take(index + 1).asBackstack()
            }
        }) {
            Text("Back to step 2")
        }

        // closeAndReplaceWith closes this screen and opens the new screen in one
        // operation. The entries below this screen stay on the backstack.
        Button(onClick = { navigation.closeAndReplaceWith(StackStep(step + 100)) }) {
            Text("Replace this step with step ${step + 100}")
        }

        // Clear and replace: the new backstack contains no entry from the old
        // backstack, so Back on the new screen does not return to a step.
        Button(onClick = { container.updateBackstack { backstackOf(StackFinished.asInstance()) } }) {
            Text("Finish: clear the stack")
        }
    }
}

@Composable
@NavigationDestination(StackFinished::class)
fun StackFinishedDestination() {
    val container = LocalNavigationContainer.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Finished", style = MaterialTheme.typography.titleLarge)
        Text("The steps were removed from the stack. This is the only entry.")
        Button(onClick = { container.updateBackstack { backstackOf(StackStep(1).asInstance()) } }) {
            Text("Start again")
        }
    }
}
