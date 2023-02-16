package dev.enro.example

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.example.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleComposableKey : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

@Composable
@NavigationDestination(ExampleComposableKey::class)
fun ExampleComposable() {
    val navigation = navigationHandle<NavigationKey>()

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