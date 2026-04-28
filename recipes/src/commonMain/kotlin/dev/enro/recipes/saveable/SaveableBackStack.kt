/**
 * Enro Recipe: Saveable Back Stack
 *
 * Demonstrates how Enro's rememberNavigationContainer automatically saves and
 * restores the entire backstack.
 */
package dev.enro.recipes.saveable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object SaveableRecipe : NavigationKey

@Serializable
data object FormScreen : NavigationKey

@Serializable
data object ReviewScreen : NavigationKey

@Composable
@NavigationDestination(SaveableRecipe::class)
fun SaveableRecipeScreen() {
    val navigation = navigationHandle<SaveableRecipe>()
    RecipeScaffold(
        title = "Saveable Back Stack",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(FormScreen.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(FormScreen::class)
fun FormScreenDestination() {
    val navigation = navigationHandle<FormScreen>()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Fill out the form")

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
        )

        Button(
            onClick = { navigation.open(ReviewScreen) },
            enabled = name.isNotBlank() && email.isNotBlank(),
        ) {
            Text("Review")
        }
    }
}

@Composable
@NavigationDestination(ReviewScreen::class)
fun ReviewScreenDestination() {
    val navigation = navigationHandle<ReviewScreen>()
    var tapCount by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Review your submission")
        Text("You've tapped the button $tapCount times")

        Button(onClick = { tapCount++ }) {
            Text("Tap me")
        }

        Button(onClick = { navigation.close() }) {
            Text("Go Back to Form")
        }
    }
}
