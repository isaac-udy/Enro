package dev.enro.core.destinations

import android.os.Parcelable
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.TestComposable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.destination.compose.ComposableDestination
import dev.enro.destination.compose.dialog.DialogDestination
import dev.enro.destination.compose.navigationHandle
import dev.enro.core.requestClose
import dev.enro.core.result.NavigationResultChannel
import dev.enro.core.result.registerForNavigationResult
import dev.enro.android.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize
import java.util.*

object ComposableDestinations {
    @Parcelize
    data class Root(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent

    @Parcelize
    data class Pushable(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>

    @Parcelize
    data class Presentable(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent.WithResult<TestResult>

    @Parcelize
    data class PresentableDialog(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent.WithResult<TestResult>

    @Parcelize
    data class PushesToPrimary(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>, TestDestination.IntoPrimaryContainer

    @Parcelize
    data class PushesToSecondary(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>, TestDestination.IntoSecondaryContainer

    @Parcelize
    data class PushesToChildAsPrimary(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>, TestDestination.IntoPrimaryChildContainer

    @Parcelize
    data class PushesToChildAsSecondary(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush.WithResult<TestResult>,
        TestDestination.IntoSecondaryChildContainer

    @Parcelize
    data class ManuallyBound(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPush, TestDestination.IntoPrimaryContainer

    // This type is not actually used in any tests at present, but just exists to prove
    // that generic navigation destinations will correctly generate code
    @Parcelize
    data class Generic<Type: Parcelable>(
        val item: Type
    ) : NavigationKey.SupportsPresent

    class TestViewModel : ViewModel() {
        private val navigation by navigationHandle<NavigationKey>()
        val resultChannel by registerForNavigationResult<TestResult> {
            navigation.registerTestResult(it)
        }
    }
}

val ComposableDestination.resultChannel: NavigationResultChannel<TestResult, NavigationKey.WithResult<TestResult>>
    get() {
        lateinit var resultChannel: NavigationResultChannel<TestResult, NavigationKey.WithResult<TestResult>>
         InstrumentationRegistry.getInstrumentation().runOnMainSync {
             resultChannel = ViewModelProvider(viewModelStore, defaultViewModelProviderFactory)
                .get(ComposableDestinations.TestViewModel::class.java)
                .resultChannel
        }
        return resultChannel
    }

@Composable
@NavigationDestination(ComposableDestinations.Root::class)
fun ComposableDestinationRoot() {
    viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Root",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
    )
}

@Composable
@NavigationDestination(ComposableDestinations.Pushable::class)
fun ComposableDestinationPushable() {
    viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushable",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
    )
}

@Composable
@NavigationDestination(ComposableDestinations.Presentable::class)
fun ComposableDestinationPresentable() {
    viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Presentable",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
    )
}

@Composable
@NavigationDestination(ComposableDestinations.PresentableDialog::class)
fun DialogDestination.ComposableDestinationPresentableDialog() {
    val navigation = navigationHandle()
    viewModel<ComposableDestinations.TestViewModel>()
    Dialog(onDismissRequest = { navigation.requestClose() }) {
        Card {
            TestComposable(
                name = "ComposableDestination Presentable Dialog",
                primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
                secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
            )
        }
    }
}

@Composable
@NavigationDestination(ComposableDestinations.PushesToPrimary::class)
fun ComposableDestinationPushesToPrimary() {
    viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushes To Primary",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryChildContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryChildContainer }
    )
}

@Composable
@NavigationDestination(ComposableDestinations.PushesToSecondary::class)
fun ComposableDestinationPushesToSecondary() {
    viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushes To Secondary",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryChildContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryChildContainer }
    )
}

@Composable
@NavigationDestination(ComposableDestinations.PushesToChildAsPrimary::class)
fun ComposableDestinationPushesToChildAsPrimary() {
    viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushes To Child As Primary"
    )
}

@Composable
@NavigationDestination(ComposableDestinations.PushesToChildAsSecondary::class)
fun ComposableDestinationPushesToChildAsSecondary() {
    viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushes To Child As Secondary"
    )
}

// Is manually bound to `ComposeDestinations.ManuallyBound`
@Composable
fun ManuallyBoundComposableScreen() {
    viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(name = "ManuallyDefinedComposable")
}

@Composable
@NavigationDestination(ComposableDestinations.Generic::class)
fun GenericComposableScreen() {
    viewModel<ComposableDestinations.TestViewModel>()
    val navigation = navigationHandle<ComposableDestinations.Generic<*>>()
    TestComposable(name = "GenericComposable\n${navigation.key.item}")
}

