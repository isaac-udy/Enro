package dev.enro.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
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
import dev.enro.core.forward
import dev.enro.core.replace
import dev.enro.core.replaceRoot
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComposeSimpleExampleKey(
    val name: String,
    val launchedFrom: String,
    val backstack: List<String> = emptyList()
) : NavigationKey

@Composable
@NavigationDestination(ComposeSimpleExampleKey::class)
fun ComposeSimpleExample() {
    val navigation = navigationHandle<ComposeSimpleExampleKey>()
    EnroExampleTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
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

                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    OutlinedButton(
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                        onClick = {
                            val next = ComposeSimpleExampleKey(
                                name = navigation.key.getNextDestinationName(),
                                launchedFrom = navigation.key.name,
                                backstack = navigation.key.backstack + navigation.key.name
                            )
                            navigation.forward(next)
                        }) {
                        Text("Forward")
                    }

                    OutlinedButton(
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                        onClick = {
                            val next = SimpleExampleKey(
                                name = navigation.key.getNextDestinationName(),
                                launchedFrom = navigation.key.name,
                                backstack = navigation.key.backstack + navigation.key.name
                            )
                            navigation.forward(next)
                        }) {
                        Text("Forward (Fragment)")
                    }

                    OutlinedButton(
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                        onClick = {
                            val next = ComposeSimpleExampleKey(
                                name = navigation.key.getNextDestinationName(),
                                launchedFrom = navigation.key.name,
                                backstack = navigation.key.backstack
                            )
                            navigation.replace(next)
                        }) {
                        Text("Replace")
                    }

                    OutlinedButton(
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                        onClick = {
                            val next = ComposeSimpleExampleKey(
                                name = navigation.key.getNextDestinationName(),
                                launchedFrom = navigation.key.name,
                                backstack = emptyList()
                            )
                            navigation.replaceRoot(next)

                        }) {
                        Text("Replace Root")
                    }
                }
            }
        }
    }
}

private fun ComposeSimpleExampleKey.getNextDestinationName(): String {
    if (name.length != 1) return "A"
    return (name[0] + 1).toString()
}