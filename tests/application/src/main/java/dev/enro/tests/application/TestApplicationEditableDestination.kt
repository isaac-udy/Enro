package dev.enro.tests.application

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

@Parcelize
internal class TestApplicationEditableDestination : NavigationKey.SupportsPush

@Composable
@NavigationDestination(TestApplicationEditableDestination::class)
internal fun TestApplicationEditableScreen() {
    Text("Test Screen")
}