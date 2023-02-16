package dev.enro.example

import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.example.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleComposableKey : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

@Composable
@NavigationDestination(ExampleComposableKey::class)
fun ExampleComposableScreen() {
    ExampleScreenTemplate("Composable")
}