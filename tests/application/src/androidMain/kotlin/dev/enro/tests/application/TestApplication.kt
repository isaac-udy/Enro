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
import dev.enro.core.NavigationDirection
import dev.enro.core.controller.NavigationApplication
import dev.enro.destination.compose.navigationContext
import dev.enro.destination.fragment.FragmentSharedElements
import dev.enro.tests.application.activity.PictureInPicture
import dev.enro.tests.application.activity.SimpleActivity
import dev.enro.tests.application.compose.BottomNavigation
import dev.enro.tests.application.compose.BottomSheetChangeSize
import dev.enro.tests.application.compose.CloseLandingPageAndPresent
import dev.enro.tests.application.compose.ComposeAnimations
import dev.enro.tests.application.compose.ComposeSavePrimitives
import dev.enro.tests.application.compose.ComposeStability
import dev.enro.tests.application.compose.EmbeddedDestination
import dev.enro.tests.application.compose.FindContext
import dev.enro.tests.application.compose.LazyColumn
import dev.enro.tests.application.compose.SyntheticViewModelAccess
import dev.enro.tests.application.fragment.FragmentPresentation
import dev.enro.tests.application.fragment.FragmentSharedElementDestination
import dev.enro.tests.application.fragment.UnboundBottomSheet
import dev.enro.tests.application.managedflow.ManagedFlowInComposable
import dev.enro.tests.application.managedflow.ManagedFlowInFragment

class TestApplication : Application(), NavigationApplication {
    override val navigationController = EnroComponent.installNavigationController(this) {
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

    override fun onCreate() {
        super.onCreate()
        SelectDestination.registerSelectableDestinations(
            BottomNavigation,
            BottomSheetChangeSize,
            CloseLandingPageAndPresent,
            ComposeAnimations,
            ComposeSavePrimitives,
            ComposeStability,
            EmbeddedDestination,
            FindContext,
            FragmentPresentation,
            FragmentSharedElementDestination,
            LazyColumn,
            ManagedFlowInComposable,
            ManagedFlowInFragment,
            PictureInPicture,
            SimpleActivity,
            SyntheticViewModelAccess,
            UnboundBottomSheet,
        )
    }
}