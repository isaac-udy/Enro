package dev.enro.example

import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

@Parcelize
object Learning : NavigationKey.SupportsPush

@Composable
@NavigationDestination(Learning::class)
fun LearningScreen() {

}