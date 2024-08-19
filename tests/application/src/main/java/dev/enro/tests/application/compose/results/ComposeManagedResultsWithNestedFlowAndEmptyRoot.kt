package dev.enro.tests.application.compose.results

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.result.deliverResultFromPush
import dev.enro.core.result.flows.registerForFlowResult
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.viewmodel.navigationHandle
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize

/**
 * A bug in Enro was identified, where a managed flow that has no root would not correctly work when navigating to a nested flow
 * that uses deliverResultForPush/Present.
 *
 * The issue is described as follows:
 * 1. Create a managed flow with a "CloseParent" empty behaviour
 * 2. Before the first navigation event, perform an async action to get some information
 * 3. The first step should navigate to an embedded flow
 * 4. When the final screen of the embedded flow returns a result, the parent flow will close (but it shouldn't)
 *
 * This bug was fixed by ensuring that the ForwardingResultInterceptor does not automatically close the original destination
 * that called deliverResultForPush/Present if that destination was launched in a managed flow.
 */
@Parcelize
object ComposeManagedResultsWithNestedFlowAndEmptyRoot : NavigationKey.SupportsPush.WithResult<String> {

    @Parcelize
    internal class NestedFlow : NavigationKey.SupportsPush.WithResult<String> {
        @Parcelize
        internal class StepOne : NavigationKey.SupportsPush.WithResult<String>

        @Parcelize
        internal class StepTwo : NavigationKey.SupportsPush.WithResult<String>
    }

    @Parcelize
    internal class StepTwo : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class FinalScreen : NavigationKey.SupportsPush.WithResult<String>

    @OptIn(ExperimentalEnroApi::class, AdvancedEnroApi::class)
    internal class FlowViewModel(
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val navigation by navigationHandle<ComposeManagedResultsWithNestedFlowAndEmptyRoot>()
        private val flow by registerForFlowResult(
            savedStateHandle = savedStateHandle,
            flow = {
                val started = async { getAsyncStarter() }
                val nestedResult = push { NestedFlow() }
                val secondResult = push { StepTwo() }
                val finalResult = push { FinalScreen() }
                return@registerForFlowResult "$started\n$nestedResult\n$secondResult\n$finalResult"
            },
            onCompleted = { result ->
                navigation.closeWithResult(result)
            }
        )

        private suspend fun getAsyncStarter(): String {
            delay(350)
            return "Flow Started:"
        }
    }
}

@NavigationDestination(ComposeManagedResultsWithNestedFlowAndEmptyRoot::class)
@Composable
internal fun ComposeManagedResultsWithNestedFlowAndEmptyRootDestination(
    viewModel: ComposeManagedResultsWithNestedFlowAndEmptyRoot.FlowViewModel = viewModel()
) {
    val container = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.CloseParent,
    )
    TitledColumn(
        title = "Results with Nested Flow and Empty Root"
    ) {
        Box(Modifier.fillMaxSize()) {
            container.Render()
            if (container.backstack.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
@NavigationDestination(ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow::class)
internal fun CmrwnfNestedFlowDestination() {
    val navigation = navigationHandle<ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow>()
    TitledColumn(
        title = "Nested Flow"
    ) {
        Button(onClick = {
            navigation.deliverResultFromPush(ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow.StepOne())
        }) {
            Text(text = "Next (to nested step one)")
        }
    }
}

@Composable
@NavigationDestination(ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow.StepOne::class)
internal fun CmrwnfNestedStepOneDestination() {
    val navigation = navigationHandle<ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow.StepOne>()
    TitledColumn(
        title = "Nested Step One"
    ) {
        Button(onClick = {
            navigation.deliverResultFromPush(ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow.StepTwo())
        }) {
            Text(text = "Next (to nested step two)")
        }
    }
}

@Composable
@NavigationDestination(ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow.StepTwo::class)
internal fun CmrwnfNestedStepTwoDestination() {
    val navigation = navigationHandle<ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow.StepTwo>()
    TitledColumn(
        title = "Nested Step Two"
    ) {
        Button(onClick = {
            navigation.closeWithResult("Cow")
        }) {
            Text(text = "Cow")
        }
        Button(onClick = {
            navigation.closeWithResult("Sheep")
        }) {
            Text(text = "Sheep")
        }
    }
}

@Composable
@NavigationDestination(ComposeManagedResultsWithNestedFlowAndEmptyRoot.StepTwo::class)
internal fun CmrwnfFlowStepTwoDestination() {
    val navigation = navigationHandle<ComposeManagedResultsWithNestedFlowAndEmptyRoot.StepTwo>()
    TitledColumn(
        title = "Step Two"
    ) {
        Button(onClick = {
            navigation.closeWithResult("House")
        }) {
            Text(text = "House")
        }
        Button(onClick = {
            navigation.closeWithResult("Farm")
        }) {
            Text(text = "Farm")
        }
    }
}

@Composable
@NavigationDestination(ComposeManagedResultsWithNestedFlowAndEmptyRoot.FinalScreen::class)
internal fun CmrwnfFlowFinalScreenDestination() {
    val navigation = navigationHandle<ComposeManagedResultsWithNestedFlowAndEmptyRoot.FinalScreen>()
    TitledColumn(
        title = "Final Screen"
    ) {
        Button(onClick = {
            navigation.closeWithResult("End")
        }) {
            Text(text = "End")
        }
    }
}

