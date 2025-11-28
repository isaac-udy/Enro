package dev.enro.tests.application

import android.app.Application
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.NavigationModule
import dev.enro.controller.internalCreateEnroController
import dev.enro.installNavigationController
import dev.enro.tests.application.activity.PictureInPicture
import dev.enro.tests.application.activity.SimpleActivity
import dev.enro.tests.application.compose.BottomNavigation
import dev.enro.tests.application.compose.BottomSheetChangeSize
import dev.enro.tests.application.compose.BottomSheetCloseAndPresent
import dev.enro.tests.application.compose.CloseLandingPageAndPresent
import dev.enro.tests.application.compose.ComposeAnimations
import dev.enro.tests.application.compose.ComposeAnimationsDestination
import dev.enro.tests.application.compose.ComposeSavePrimitives
import dev.enro.tests.application.compose.ComposeStability
import dev.enro.tests.application.compose.EmbeddedDestination
import dev.enro.tests.application.compose.FindContext
import dev.enro.tests.application.compose.LazyColumn
import dev.enro.tests.application.compose.SyntheticViewModelAccess
import dev.enro.tests.application.fragment.FragmentPresentation
import dev.enro.tests.application.fragment.UnboundBottomSheet
import dev.enro.tests.application.managedflow.ManagedFlowInComposable
import dev.enro.tests.application.savedstate.SavedStateActivity
import dev.enro.tests.application.savedstate.SavedStateDestination
import dev.enro.tests.application.serialization.AndroidSerialization

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        SelectDestination.registerSelectableDestinations(
            AndroidSerialization,
            BottomNavigation,
            BottomSheetChangeSize,
            BottomSheetCloseAndPresent,
            CloseLandingPageAndPresent,
            ComposeAnimations,
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

        installNavigationController(this)
//        val thing = ::ComposeAnimationsDestination
//        val thing2 = ::SavedStateActivity
//        val thing3 = ::composeStabilityDestination
//        TestApplicationComponent.installNavigationController(this)
    }
}