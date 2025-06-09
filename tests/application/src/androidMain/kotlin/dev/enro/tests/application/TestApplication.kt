package dev.enro.tests.application

import android.app.Application
import dev.enro.controller.internalCreateEnroController
import dev.enro.tests.application.activity.PictureInPicture
import dev.enro.tests.application.activity.SimpleActivity
import dev.enro.tests.application.compose.BottomNavigation
import dev.enro.tests.application.compose.BottomSheetChangeSize
import dev.enro.tests.application.compose.BottomSheetCloseAndPresent
import dev.enro.tests.application.compose.CloseLandingPageAndPresent
import dev.enro.tests.application.compose.ComposeAnimations
import dev.enro.tests.application.compose.ComposeSavePrimitives
import dev.enro.tests.application.compose.ComposeStability
import dev.enro.tests.application.compose.EmbeddedDestination
import dev.enro.tests.application.compose.FindContext
import dev.enro.tests.application.compose.LazyColumn
import dev.enro.tests.application.compose.SyntheticViewModelAccess
import dev.enro.tests.application.compose.results.ComposeEmbeddedResultFlow
import dev.enro.tests.application.fragment.FragmentPresentation
import dev.enro.tests.application.fragment.UnboundBottomSheet
import dev.enro.tests.application.managedflow.ManagedFlowInComposable
import dev.enro.tests.application.savedstate.SavedStateDestination
import dev.enro.tests.application.serialization.AndroidSerialization

class TestApplication : Application() {
//    override val navigationController = EnroComponent.installNavigationController(
//        application = this,
//        backConfiguration = EnroBackConfiguration.Predictive,
//    ) {
//        plugin(TestApplicationPlugin)
//        composeEnvironment { content ->
//            val navigationContext = navigationContext
//            val isRoot = remember(navigationContext) {
//                val parent = navigationContext.parentContext?.parentContext
//                return@remember parent == null || parent.contextReference is Activity
//            }
//            if (isRoot) {
//                MaterialTheme {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        Box(
//                            modifier = Modifier.fillMaxSize(1f)
//                                .windowInsetsPadding(WindowInsets.navigationBars)
//                                .windowInsetsPadding(WindowInsets.statusBars)
//                        ) {
//                            content()
//                        }
//                    }
//                }
//            }
//            else {
//                content()
//            }
//        }
//
//        /**
//         * The following plugin is installed specifically to support the example in
//         *  [dev.enro.tests.application.fragment.FragmentSharedElementDestination], which has an example of
//         *  shared element transitions between a Fragment and Composable NavigationDestination
//         */
//        plugin(FragmentSharedElements.composeCompatibilityPlugin)
//    }

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
            FragmentPresentation,
            LazyColumn,
            ManagedFlowInComposable,
            PictureInPicture,
            SavedStateDestination,
            SimpleActivity,
            SyntheticViewModelAccess,
            UnboundBottomSheet,
        )

        internalCreateEnroController {
            NavigationComponentNavigation().apply { invoke() }
        }.install(this)
    }
}