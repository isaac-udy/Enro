package dev.enro.example

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.navigationHandle
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
class ExampleComposableKey : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

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
    val navigation = navigationHandle<NavigationKey>()
    val viewModel = viewModel<ComposeSimpleExampleViewModel>()
    ExampleScreenTemplate(
        title = "Composable",
        buttons = listOf(
            "Forward" to {
                val next = ExampleComposableKey()
                navigation.forward(next)
            },
            "Forward (Fragment)" to {
                val next = ExampleFragmentKey()
                navigation.forward(next)
            },
            "Replace" to {
                val next = ExampleComposableKey()
                navigation.replace(next)
            },
            "Replace Root" to {
                val next = ExampleComposableKey()
                navigation.replaceRoot(next)
            },
            "Bottom Sheet" to {
                val next = ExampleComposableKey()
                navigation.present(ExampleComposableBottomSheetKey())
            },
        )
    )
}

@Parcelize
class ExampleComposableBottomSheetKey : NavigationKey.SupportsPresent

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(ExampleComposableBottomSheetKey::class)
fun BottomSheetDestination.ExampleDialogComposable() {
    val navigation = navigationHandle<NavigationKey>()

    ExampleScreenTemplate(
        title = "Composable",
        modifier = Modifier.padding(
            top = 16.dp,
            start = 16.dp,
            end = 16.dp
        ),
        buttons = listOf(
            "Forward" to {
                val next = ExampleComposableKey()
                navigation.forward(next)
            },
            "Forward (Fragment)" to {
                val next = ExampleFragmentKey()
                navigation.forward(next)
            },
            "Replace" to {
                val next = ExampleComposableKey()
                navigation.replace(next)
            },
            "Replace Root" to {
                val next = ExampleComposableKey()
                navigation.replaceRoot(next)
            },
            "Bottom Sheet" to {
                val next = ExampleComposableKey()
                navigation.present(ExampleComposableBottomSheetKey())
            },
        )
    )
}