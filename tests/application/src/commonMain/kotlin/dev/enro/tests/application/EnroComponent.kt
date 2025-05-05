package dev.enro.tests.application

import dev.enro.animation.NavigationAnimationForComposable
import dev.enro.animation.SlideDefaults
import dev.enro.annotations.NavigationComponent
import dev.enro.core.NavigationComponentConfiguration
import dev.enro.core.controller.createNavigationModule


private val maximumWidthForSlide = 1080

@NavigationComponent
object EnroComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        animations {
            defaults(NavigationAnimationForComposable.SlideDefaults)
        }
    }
)