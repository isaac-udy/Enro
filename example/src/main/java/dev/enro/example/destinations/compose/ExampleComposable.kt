package dev.enro.example.destinations.compose

import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.example.core.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleComposable : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

@Composable
@NavigationDestination(ExampleComposable::class)
fun ExampleComposableScreen() {
    ExampleScreenTemplate("Composable")
}