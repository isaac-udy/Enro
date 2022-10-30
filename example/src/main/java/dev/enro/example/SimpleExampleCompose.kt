package dev.enro.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.present
import dev.enro.core.push
import kotlinx.parcelize.Parcelize

@Parcelize
data class SimpleExampleComposeKey(
    val name: String,
    val launchedFrom: String,
    val backstack: List<String> = emptyList()
) : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

@Composable
@NavigationDestination(SimpleExampleComposeKey::class)
fun SimpleExampleComposeScreen() {
    val navigation = navigationHandle<SimpleExampleComposeKey>()

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
        ) {
            Column {
                Text(
                    text = "Example Composable",
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(R.string.example_content),
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Current Destination:",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = navigation.key.name,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Launched From:",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = navigation.key.launchedFrom,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Current Stack:",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = (navigation.key.backstack + navigation.key.name).joinToString(" -> "),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .padding(vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val next = SimpleExampleFragmentKey(
                            name = navigation.key.getNextDestinationName(),
                            launchedFrom = navigation.key.name,
                            backstack = navigation.key.backstack + navigation.key.name
                        )
                        navigation.push(next)
                    }) {
                    Text("Push (Fragment)")
                }

                OutlinedButton(
                    onClick = {
                        val next = SimpleExampleComposeKey(
                            name = navigation.key.getNextDestinationName(),
                            launchedFrom = navigation.key.name,
                            backstack = navigation.key.backstack + navigation.key.name
                        )
                        navigation.push(next)
                    }) {
                    Text("Push")
                }

                OutlinedButton(
                    onClick = {
                        val next = SimpleExampleFragmentKey(
                            name = navigation.key.getNextDestinationName(),
                            launchedFrom = navigation.key.name,
                            backstack = navigation.key.backstack
                        )
                        navigation.present(next)
                    }) {
                    Text("Present (Fragment)")
                }

                OutlinedButton(
                    onClick = {
                        val next = SimpleExampleComposeKey(
                            name = navigation.key.getNextDestinationName(),
                            launchedFrom = navigation.key.name,
                            backstack = emptyList()
                        )
                        navigation.present(next)
                    }) {
                    Text("Present")
                }
            }
        }
    }
}

private fun SimpleExampleComposeKey.getNextDestinationName(): String {
    if (name.length != 1) return "A"
    return (name[0] + 1).toString()
}