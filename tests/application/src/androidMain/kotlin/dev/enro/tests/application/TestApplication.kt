package dev.enro.tests.application

import android.app.Activity
import android.app.Application
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.enro.animation.direction
import dev.enro.annotations.NavigationComponent
import dev.enro.core.NavigationDirection
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.createNavigationController
import dev.enro.core.navigationContext
import dev.enro.destination.fragment.FragmentSharedElements

@NavigationComponent
class TestApplication : Application(), NavigationApplication {
    override val navigationController = createNavigationController {
        plugin(TestApplicationPlugin)
        composeEnvironment { content ->
            val navigationContext = navigationContext
            val isRoot = remember(navigationContext) {
                val parent = navigationContext.parentContext?.parentContext
                return@remember parent == null || parent.contextReference is Activity
            }
            if (isRoot) {
                MaterialTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier.fillMaxSize(1f)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .windowInsetsPadding(WindowInsets.statusBars)
                        ) {
                            content()
                        }
                    }
                }
            }
            else {
                content()
            }
        }

        /**
         * The following plugin is installed specifically to support the example in
         *  [dev.enro.tests.application.fragment.FragmentSharedElementDestination], which has an example of
         *  shared element transitions between a Fragment and Composable NavigationDestination
         */
        plugin(FragmentSharedElements.composeCompatibilityPlugin)

        animations {
            direction(
                direction = NavigationDirection.Push,
                entering = fadeIn() + slideInHorizontally { it / 3 },
                exiting = slideOutHorizontally { -it / 6 },
                returnEntering = slideInHorizontally { -it / 6 },
                returnExiting = fadeOut() + slideOutHorizontally { it / 3 }
            )
        }
    }
}