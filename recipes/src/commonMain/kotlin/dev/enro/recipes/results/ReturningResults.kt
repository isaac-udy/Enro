/**
 * Enro Recipe: Returning Results
 *
 * Demonstrates how Enro handles typed results between screens.
 */
package dev.enro.recipes.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ResultsRecipe : NavigationKey

@Serializable
data object ResultsHome : NavigationKey

@Serializable
data class EnterText(val prompt: String) : NavigationKey.WithResult<String>

@Serializable
data class SelectColor(val colors: List<String>) : NavigationKey.WithResult<String>

@Serializable
data class ConfirmAction(val message: String) : NavigationKey.WithResult<Boolean>

@Composable
@NavigationDestination(ResultsRecipe::class)
fun ResultsRecipeScreen() {
    val navigation = navigationHandle<ResultsRecipe>()
    RecipeScaffold(
        title = "Returning Results",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(ResultsHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(ResultsHome::class)
fun ResultsHomeDestination() {
    var enteredText by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedColor by rememberSaveable { mutableStateOf<String?>(null) }
    var isConfirmed by rememberSaveable { mutableStateOf<Boolean?>(null) }

    val getTextResult = registerForNavigationResult<String>(
        onClosed = { },
        onCompleted = { result ->
            enteredText = result
        },
    )

    val getColorResult = registerForNavigationResult<String> { color ->
        selectedColor = color
    }

    val getConfirmation = registerForNavigationResult<Boolean> { confirmed ->
        isConfirmed = confirmed
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Results Demo")
        Text("Text: ${enteredText ?: "not entered"}")
        Text("Color: ${selectedColor ?: "not selected"}")
        Text("Confirmed: ${isConfirmed ?: "not answered"}")

        Button(onClick = { getTextResult.open(EnterText("Enter your name")) }) {
            Text("Enter Text")
        }

        Button(onClick = { getColorResult.open(SelectColor(listOf("Red", "Green", "Blue"))) }) {
            Text("Select Color")
        }

        Button(onClick = { getConfirmation.open(ConfirmAction("Delete all items?")) }) {
            Text("Confirm Action")
        }
    }
}

@Composable
@NavigationDestination(EnterText::class)
fun EnterTextDestination() {
    val navigation = navigationHandle<EnterText>()
    var text by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(navigation.key.prompt)

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { navigation.complete(text) },
            enabled = text.isNotBlank(),
        ) {
            Text("Submit")
        }

        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}

@Composable
@NavigationDestination(SelectColor::class)
fun SelectColorDestination() {
    val navigation = navigationHandle<SelectColor>()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Select a color:")
        navigation.key.colors.forEach { color ->
            Button(onClick = { navigation.complete(color) }) {
                Text(color)
            }
        }
        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}

@Composable
@NavigationDestination(ConfirmAction::class)
fun ConfirmActionDestination() {
    val navigation = navigationHandle<ConfirmAction>()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(navigation.key.message)
        Button(onClick = { navigation.complete(true) }) {
            Text("Yes")
        }
        Button(onClick = { navigation.complete(false) }) {
            Text("No")
        }
    }
}
