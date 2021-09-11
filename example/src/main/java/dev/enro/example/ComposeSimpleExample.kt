package dev.enro.example

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.enro.annotations.ExperimentalComposableDestination
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.*
import dev.enro.core.compose.dialog.*
import dev.enro.core.result.closeWithResult
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingletonThing @Inject constructor() {
    val id = UUID.randomUUID().toString()
}

class ThingThing @Inject constructor() {
    val id = UUID.randomUUID().toString()
}

@Parcelize
data class ComposeSimpleExampleKey(
    val name: String,
    val launchedFrom: String,
    val backstack: List<String> = emptyList()
) : NavigationKey

@HiltViewModel
class ComposeSimpleExampleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val singletonThing: SingletonThing,
    private val thingThing: ThingThing
) : ViewModel() {

    init {
        val isRestored = savedStateHandle.contains("savedId")
        val savedId =  savedStateHandle.get<String>("savedId") ?: UUID.randomUUID().toString()
        savedStateHandle.set("savedId", savedId)
        Log.e("CSEVM", "Opened $savedId/${singletonThing.id}/${thingThing.id} (was restored $isRestored)")
    }

}

@Composable
@ExperimentalComposableDestination
@NavigationDestination(ComposeSimpleExampleKey::class)
fun ComposeSimpleExample() {

    val navigation = navigationHandle<ComposeSimpleExampleKey>()
    val scrollState = rememberScrollState()
    val viewModel = viewModel<ComposeSimpleExampleViewModel>()

    EnroExampleTheme {
        Surface {
            val topContentHeight = remember { mutableStateOf(0)}
            val bottomContentHeight = remember { mutableStateOf(0)}
            val availableHeight = remember { mutableStateOf(0)}
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
                    .onGloballyPositioned { availableHeight.value = it.size.height },
            ) {
                Column(
                    modifier = Modifier.onGloballyPositioned { topContentHeight.value = it.size.height }
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
                }

                val density = LocalDensity.current
                Spacer(modifier = Modifier.height(
                    if(scrollState.maxValue == 0) (availableHeight.value - topContentHeight.value - bottomContentHeight.value).div(density.density).dp - 1.dp else 0.dp
                ))

                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .onGloballyPositioned { bottomContentHeight.value = it.size.height }
                        .padding(top = 16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                        onClick = {
                            val next = ComposeSimpleExampleKey(
                                name = navigation.key.getNextDestinationName(),
                                launchedFrom = navigation.key.name,
                                backstack = navigation.key.backstack + navigation.key.name
                            )
                            navigation.forward(ExampleComposableDialogKey())
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

@Parcelize
class ExampleComposableDialogKey : NavigationKey

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
@ExperimentalComposableDestination
@NavigationDestination(ExampleComposableDialogKey::class)
fun BottomSheetDestination.ExampleDialogComposable() {
    val navigationHandle = navigationHandle()
    val closeConfirmation = registerForNavigationResult<Boolean> {
        if(it) {
            navigationHandle.close()
        }
    }
    navigationHandle.configure {
        onCloseRequested { closeConfirmation.open(ExampleConfirmComposableKey()) }
    }

    LazyColumn {
        items(50) {
            ListItem(
                text = { Text("Item $it") },
                icon = {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Localized description"
                    )
                }
            )
        }
    }
}

@Parcelize
class ExampleConfirmComposableKey : NavigationKey.WithResult<Boolean>

@Composable
@ExperimentalComposableDestination
@NavigationDestination(ExampleConfirmComposableKey::class)
fun DialogDestination.ExampleConfirmComposable() {
    val navigationHandle = navigationHandle<ExampleConfirmComposableKey>()
    AlertDialog(
        onDismissRequest = { navigationHandle.close() },
        buttons = {
            TextButton(onClick = {
                navigationHandle.close()
            }) {
                Text("Close")
            }
            TextButton(onClick = {
                navigationHandle.closeWithResult(true)
            }) {
                Text("Confirm")
            }
        },
        title = { Text("Confirm Close") }
    )
}

private fun ComposeSimpleExampleKey.getNextDestinationName(): String {
    if (name.length != 1) return "A"
    return (name[0] + 1).toString()
}