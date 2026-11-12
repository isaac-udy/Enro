// Snippets for docs/ghpages/docs/getting-started/your-first-screen.md
package dev.enro.docs.gettingstarted.firstscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import kotlinx.serialization.Serializable

// region docs:your-first-screen/keys
@Serializable
data object Home : NavigationKey

@Serializable
data class Profile(val userId: String) : NavigationKey

@Serializable
data object PickName : NavigationKey.WithResult<String>
// endregion

// region docs:your-first-screen/destinations
@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    val navigation = navigationHandle<Home>()

    val askForName = registerForNavigationResult<String>(
        onCompleted = { name -> /* we'll use it in a moment */ },
    )

    Column {
        Text("Welcome home")

        Button(onClick = { navigation.open(Profile("user-123")) }) {
            Text("View profile")
        }

        Button(onClick = { askForName.open(PickName) }) {
            Text("Pick a name")
        }
    }
}

@Composable
@NavigationDestination(Profile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<Profile>()

    Column {
        Text("Profile for ${navigation.key.userId}")
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

@Composable
@NavigationDestination(PickName::class)
fun PickNameScreen() {
    val navigation = navigationHandle<PickName>()
    var input by remember { mutableStateOf("") }

    Column {
        TextField(value = input, onValueChange = { input = it })

        Button(onClick = { navigation.complete(input) }) {
            Text("Done")
        }
        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}
// endregion

// region docs:your-first-screen/component
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { }
)
// endregion
