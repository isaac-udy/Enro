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
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.completeFrom
import dev.enro.navigationHandle
import dev.enro.result.flow.registerForFlowResult
import dev.enro.result.flow.rememberNavigationContainerForFlow
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

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
@Serializable
object ComposeManagedResultsWithNestedFlowAndEmptyRoot : NavigationKey.WithResult<String> {

    @Serializable
    internal class NestedFlow : NavigationKey.WithResult<String> {
        @Serializable
        internal class StepOne : NavigationKey.WithResult<String>

        @Serializable
        internal class StepTwo : NavigationKey.WithResult<String>
    }

    @Serializable
    internal class StepTwo : NavigationKey.WithResult<String>

    @Serializable
    internal class FinalScreen : NavigationKey.WithResult<String>

    @OptIn(ExperimentalEnroApi::class, AdvancedEnroApi::class)
    internal class FlowViewModel(
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val navigation by navigationHandle<ComposeManagedResultsWithNestedFlowAndEmptyRoot>()

        val flow by registerForFlowResult(
            flow = {
                val started = async { getAsyncStarter() }
                val nestedResult = open { NestedFlow() }
                val secondResult = open { StepTwo() }
                val finalResult = open { FinalScreen() }
                return@registerForFlowResult "$started\n$nestedResult\n$secondResult\n$finalResult"
            },
            onCompleted = { result ->
                navigation.complete(result)
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
    viewModel: ComposeManagedResultsWithNestedFlowAndEmptyRoot.FlowViewModel = viewModel {
        createEnroViewModel {
            ComposeManagedResultsWithNestedFlowAndEmptyRoot.FlowViewModel(
                savedStateHandle = createSavedStateHandle(),
            )
        }
    }
) {
    val container = rememberNavigationContainerForFlow(
        flow = viewModel.flow
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
            navigation.completeFrom(ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow.StepOne())
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
            navigation.completeFrom(ComposeManagedResultsWithNestedFlowAndEmptyRoot.NestedFlow.StepTwo())
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
            navigation.complete("Cow")
        }) {
            Text(text = "Cow")
        }
        Button(onClick = {
            navigation.complete("Sheep")
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
            navigation.complete("House")
        }) {
            Text(text = "House")
        }
        Button(onClick = {
            navigation.complete("Farm")
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
            navigation.complete("End")
        }) {
            Text(text = "End")
        }
    }
}

