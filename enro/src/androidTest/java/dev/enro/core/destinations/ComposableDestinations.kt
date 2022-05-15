package dev.enro.core.destinations

import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.TestComposable
import dev.enro.annotations.ExperimentalComposableDestination
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.dialog.configureDialog
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.requestClose
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.registerForNavigationResult
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize
import java.util.*

object ComposableDestinations {
    @Parcelize
    data class Root(
        val id: String = UUID.randomUUID().toString()
    ) : NavigationKey.SupportsPresent

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

    class TestViewModel : ViewModel() {
        private val navigation by navigationHandle<NavigationKey>()
        val resultChannel by registerForNavigationResult<TestResult>(navigation) {
            navigation.registerTestResult(it)
        }
    }
}

val ComposableDestination.resultChannel: EnroResultChannel<TestResult>
    get() {
        return ViewModelProvider(viewModelStore, ViewModelProvider.NewInstanceFactory())
            .get(ComposableDestinations.TestViewModel::class.java)
            .resultChannel
    }

@Composable
@ExperimentalComposableDestination
@NavigationDestination(ComposableDestinations.Root::class)
fun ComposableDestinationRoot() {
    val viewModel = viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Root",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
    )
}

@Composable
@ExperimentalComposableDestination
@NavigationDestination(ComposableDestinations.Presentable::class)
fun ComposableDestinationPresentable() {
    val viewModel = viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Presentable",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryContainer }
    )
}

@Composable
@ExperimentalComposableDestination
@NavigationDestination(ComposableDestinations.PresentableDialog::class)
fun DialogDestination.ComposableDestinationPresentableDialog() {
    val navigation = navigationHandle()
    val viewModel = viewModel<ComposableDestinations.TestViewModel>()
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
@ExperimentalComposableDestination
@NavigationDestination(ComposableDestinations.PushesToPrimary::class)
fun ComposableDestinationPushesToPrimary() {
    val viewModel = viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushes To Primary",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryChildContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryChildContainer }
    )
}

@Composable
@ExperimentalComposableDestination
@NavigationDestination(ComposableDestinations.PushesToSecondary::class)
fun ComposableDestinationPushesToSecondary() {
    val viewModel = viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushes To Secondary",
        primaryContainerAccepts = { it is TestDestination.IntoPrimaryChildContainer },
        secondaryContainerAccepts = { it is TestDestination.IntoSecondaryChildContainer }
    )
}

@Composable
@ExperimentalComposableDestination
@NavigationDestination(ComposableDestinations.PushesToChildAsPrimary::class)
fun ComposableDestinationPushesToChildAsPrimary() {
    val viewModel = viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushes To Child As Primary"
    )
}

@Composable
@ExperimentalComposableDestination
@NavigationDestination(ComposableDestinations.PushesToChildAsSecondary::class)
fun ComposableDestinationPushesToChildAsSecondary() {
    val viewModel = viewModel<ComposableDestinations.TestViewModel>()
    TestComposable(
        name = "ComposableDestination Pushes To Child As Secondary"
    )
}