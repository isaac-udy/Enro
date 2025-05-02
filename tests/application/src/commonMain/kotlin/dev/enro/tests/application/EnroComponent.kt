package dev.enro.tests.application

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import dev.enro.animation.direction
import dev.enro.annotations.NavigationComponent
import dev.enro.core.NavigationComponentConfiguration
import dev.enro.core.NavigationDirection
import dev.enro.core.controller.createNavigationModule


private val maximumWidthForSlide = 1080

@NavigationComponent
object EnroComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        animations {
            direction(
                direction = NavigationDirection.Push,
                entering = fadeIn() + slideInHorizontally {
                    it.coerceAtMost(maximumWidthForSlide) / 3
                },
                exiting = fadeOut() + slideOutHorizontally {
                    -(it.coerceAtMost(maximumWidthForSlide)) / 6
                },
                returnEntering = fadeIn() + slideInHorizontally {
                    -(it.coerceAtMost(maximumWidthForSlide)) / 6
                },
                returnExiting = fadeOut() + slideOutHorizontally {
                    it.coerceAtMost(maximumWidthForSlide) / 3
                },
            )
        }
    }
)