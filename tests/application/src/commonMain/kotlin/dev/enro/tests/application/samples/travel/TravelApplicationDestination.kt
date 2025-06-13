package dev.enro.tests.application.samples.travel

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object TravelSample : NavigationKey

@Composable
@NavigationDestination(TravelSample::class)
fun TravelApplicationDestination() {
    val container = rememberNavigationContainer(
        backstack = listOf(LoginScreen.asInstance()),
    )
    NavigationDisplay(state = container)
}