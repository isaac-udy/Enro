// Snippets for docs/ghpages/docs/advanced/results/embedded-result-flows.md
package dev.enro.docs.advanced.results.embedded

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountResult(val name: String, val email: String)

@Serializable
data object CreateAccount : NavigationKey.WithResult<CreateAccountResult>

@Serializable
data object EnterName : NavigationKey.WithResult<String>

@Serializable
data object EnterEmail : NavigationKey.WithResult<String>

// region docs:embedded-result-flows/flow
@Composable
@NavigationDestination(CreateAccount::class)
fun CreateAccountScreen() {
    val navigation = navigationHandle<CreateAccount>()
    var name by rememberSaveable { mutableStateOf<String?>(null) }
    var email by rememberSaveable { mutableStateOf<String?>(null) }

    val askForEmail = registerForNavigationResult<String>(
        onCompleted = { result ->
            email = result
            navigation.complete(CreateAccountResult(name!!, result))
        },
    )

    val askForName = registerForNavigationResult<String>(
        onCompleted = { result ->
            name = result
            askForEmail.open(EnterEmail)
        },
    )

    Button(onClick = { askForName.open(EnterName) }) {
        Text("Start")
    }
}
// endregion
