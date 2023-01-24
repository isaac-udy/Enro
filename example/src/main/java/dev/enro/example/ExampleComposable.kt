package dev.enro.example

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberEnroContainerController
import dev.enro.core.container.EmptyBehavior
import dev.enro.example.ui.ExampleScreenTemplate
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
data class ExampleComposableKey(
    val name: String,
    val launchedFrom: String,
    val backstack: List<String> = emptyList()
) : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

@HiltViewModel
class ComposeSimpleExampleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val singletonThing: SingletonThing,
    private val thingThing: ThingThing
) : ViewModel() {

    init {
        val isRestored = savedStateHandle.contains("savedId")
        val savedId = savedStateHandle.get<String>("savedId") ?: UUID.randomUUID().toString()
        savedStateHandle.set("savedId", savedId)
    }

}

@Composable
@NavigationDestination(ExampleComposableKey::class)
fun ExampleComposable() {
    val navigation = navigationHandle<ExampleComposableKey>()
    val viewModel = viewModel<ComposeSimpleExampleViewModel>()
    ExampleScreenTemplate(
        title = "Composable",
        buttons = listOf(
            "Forward" to {
                val next = ExampleComposableKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack + navigation.key.name
                )
                navigation.forward(next)
            },
            "Forward (Fragment)" to {
                val next = ExampleFragmentKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack + navigation.key.name
                )
                navigation.forward(next)
            },
            "Replace" to {
                val next = ExampleComposableKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack
                )
                navigation.replace(next)
            },
            "Replace Root" to {
                val next = ExampleComposableKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = emptyList()
                )
                navigation.replaceRoot(next)
            },
            "Bottom Sheet" to {
                val next = ExampleComposableKey(
                    name = navigation.key.getNextDestinationName(),
                    launchedFrom = navigation.key.name,
                    backstack = navigation.key.backstack + navigation.key.name
                )
                navigation.present(ExampleComposableBottomSheetKey(NavigationInstruction.Present(next)))
            },
        )
    )
}

@Parcelize
class ExampleComposableBottomSheetKey(val innerKey: NavigationInstruction.Open<*>) : NavigationKey.SupportsPresent

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(ExampleComposableBottomSheetKey::class)
fun BottomSheetDestination.ExampleDialogComposable() {
    val navigationHandle = navigationHandle<ExampleComposableBottomSheetKey>()
    EnroContainer(
        container = rememberEnroContainerController(
            initialBackstack = listOf(navigationHandle.key.innerKey),
            accept = { false },
            emptyBehavior = EmptyBehavior.CloseParent
        )
    )
}

private fun ExampleComposableKey.getNextDestinationName(): String {
    if (name.length != 1) return "A"
    return (name[0] + 1).toString()
}