package dev.enro.example.module

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleModuleScreen : NavigationKey.SupportsPush

@Composable
@NavigationDestination(ExampleModuleScreen::class)
fun ExampleModuleDestination() {
    Text(text = "Example Module Destination")
}