package dev.enro.tests.application.samples.travel

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.tests.application.samples.travel.ui.registration.RegistrationSuccessfulDestination
import dev.enro.tests.application.samples.travel.ui.theme.TravelTheme
import dev.enro.ui.NavigationAnimations
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object TravelSampleDestination : NavigationKey

@Composable
@NavigationDestination(TravelSampleDestination::class)
fun TravelSampleScreen() {
    TravelTheme(
        darkTheme = false,
    ) {
        val container = rememberNavigationContainer(
            backstack = backstackOf(LoginDestination.asInstance()),
        )
        NavigationDisplay(
            state = container,
            animations = NavigationAnimations.Default.copy(
                transitionSpec = {
                    val visibleKey = targetState.visible.lastOrNull()?.key
                    when (visibleKey) {
                        is LoginDestination,
                        is RegistrationSuccessfulDestination,
                        is HomeDestination,
                            -> fadeIn() togetherWith fadeOut()

                        else -> NavigationAnimations.Default.transitionSpec(this)
                    }
                }
            )
        )
    }
}
