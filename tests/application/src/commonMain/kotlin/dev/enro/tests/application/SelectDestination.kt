package dev.enro.tests.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.controller.NavigationController
import dev.enro.core.present
import dev.enro.core.push
import dev.enro.destination.compose.navigationContext
import kotlinx.serialization.Serializable

@Serializable
internal object SelectDestination : NavigationKey.SupportsPush, NavigationKey.SupportsPresent

@Composable
@NavigationDestination(SelectDestination::class)
fun SelectDestinationScreen() {
    val navigation = navigationHandle()
    val isPresented = remember {
        navigation.instruction.navigationDirection == NavigationDirection.Present
    }
    val context = navigationContext
    val destinations = remember {
        loadNavigationDestinations(context.controller)
    }

    val destinationsList = remember {
        movableContentOf {
            destinations.forEach {
                ReflectedDestinationCard(reflectedDestination = it)
            }
        }
    }

    if (isPresented) {
        DialogDestination {
            Dialog(
                onDismissRequest = { navigation.close() }
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .shadow(16.dp)
                        .background(
                            color = MaterialTheme.colors.background,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Destinations",
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    destinationsList()
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Destinations",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            destinationsList()
        }
    }
}

@Composable
fun ReflectedDestinationCard(
    reflectedDestination: ReflectedDestination
) {
    val navigation = navigationHandle()
    Card(
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = reflectedDestination.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.button,
            )

            when {
                reflectedDestination.pushInstance == null -> {
                    TextButton(
                        modifier = Modifier.widthIn(min = 56.dp),
                        onClick = {
                            navigation.present(reflectedDestination.presentInstance!!)
                        }
                    ) {
                        Text("Present")
                    }
                }

                reflectedDestination.presentInstance == null -> {
                    TextButton(
                        modifier = Modifier.widthIn(min = 56.dp),
                        onClick = {
                            navigation.push(reflectedDestination.pushInstance)
                        }
                    ) {
                        Text("Push")
                    }
                }

                else -> {
                    var popUpVisible by remember { mutableStateOf(false) }
                    TextButton(
                        modifier = Modifier.widthIn(min = 56.dp),
                        onClick = { popUpVisible = true }
                    ) {
                        Text("Open")
                        if (popUpVisible) {
                            Popup(
                                alignment = Alignment.TopEnd,
                                onDismissRequest = { popUpVisible = false }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .shadow(4.dp)
                                        .background(MaterialTheme.colors.background)
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    TextButton(
                                        onClick = {
                                            popUpVisible = false
                                            navigation.present(reflectedDestination.presentInstance)
                                        }
                                    ) {
                                        Text("Present")
                                    }
                                    TextButton(
                                        onClick = {
                                            popUpVisible = false
                                            navigation.push(reflectedDestination.pushInstance)
                                        }
                                    ) {
                                        Text("Push")
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }
    }
}

data class ReflectedDestination(
    val pushInstance: NavigationKey.SupportsPush?,
    val presentInstance: NavigationKey.SupportsPresent?,
    val title: String,
)

// This is a hacky method of loading all NavigationKeys available,
// so that we can render a destination picker easily in the test application
// this uses slow reflection to work, and should not be used in a production application
internal expect fun loadNavigationDestinations(
    controller: NavigationController
): List<ReflectedDestination>