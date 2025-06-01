package dev.enro.tests.application

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.NavigationApplication
import dev.enro.destination.compose.navigationContext
import dev.enro.destination.fragment.FragmentSharedElements
import dev.enro.tests.application.activity.PictureInPicture
import dev.enro.tests.application.activity.SimpleActivity
import dev.enro.tests.application.compose.*
import dev.enro.tests.application.compose.results.ComposeEmbeddedResultFlow
import dev.enro.tests.application.fragment.FragmentAnimations
import dev.enro.tests.application.fragment.FragmentPresentation
import dev.enro.tests.application.fragment.FragmentSharedElementDestination
import dev.enro.tests.application.fragment.UnboundBottomSheet
import dev.enro.tests.application.managedflow.ManagedFlowInComposable
import dev.enro.tests.application.managedflow.ManagedFlowInFragment
import dev.enro.tests.application.serialization.AndroidSerialization
import dev.enro3.controller.internalCreateEnroController

class TestApplication : Application(), NavigationApplication {
    override val navigationController = EnroComponent.installNavigationController(
        application = this,
        backConfiguration = EnroBackConfiguration.Predictive,
    ) {
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
    }

    override fun onCreate() {
        super.onCreate()
        SelectDestination.registerSelectableDestinations(
            AndroidSerialization,
            BottomNavigation,
            BottomSheetChangeSize,
            BottomSheetCloseAndPresent,
            CloseLandingPageAndPresent,
            ComposeAnimations,
            ComposeEmbeddedResultFlow,
            ComposeSavePrimitives,
            ComposeStability,
            EmbeddedDestination,
            FindContext,
            FragmentAnimations,
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

        internalCreateEnroController {
            destination<ListKey>(listDestination)
            destination<DetailKey>(detailDestination)
            destination<ResultKey>(resultDestination)
            destination<SyntheticKey>(syntheticDestination)
            destination<FragmentKey>(fragmentDestination)
            destination<ActivityKey>(activityDestination)
            destination<ScreenWithViewModelKey>(screenWithViewModelDestination)
            destination<DialogKey>(dialogDestination)
            destination<NestedKey>(nestedDestination)
            destination<EmptyKey>(emptyDestination)
            destination<DirectDialogKey>(directDialogDestination)
            destination<DirectButtonKey>(directButtonDestination)
            destination<DirectBottomSheetKey>(directBottomSheetDestination)
        }.install(this)
    }
}