package dev.enro.tests.module

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

@Parcelize
internal class TestModuleEditableDestination : NavigationKey.SupportsPush

@Composable
@NavigationDestination(TestModuleEditableDestination::class)
internal fun TestModuleEditableScreen() {
    Text("Test Screen")
}