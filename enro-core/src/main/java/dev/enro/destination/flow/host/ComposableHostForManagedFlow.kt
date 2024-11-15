package dev.enro.destination.flow.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.core.NavigationKey
import dev.enro.core.asTyped
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.destination.navigationController
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptFromFlow
import dev.enro.destination.flow.ManagedFlowNavigationBinding
import dev.enro.destination.flow.ManagedFlowViewModel
import dev.enro.viewmodel.getNavigationHandle

internal class ComposableHostForManagedFlowDestination : ComposableDestination() {
    @Composable
    override fun Render() {
        val viewModel = viewModel<ManagedFlowViewModel>()
        val container = rememberNavigationContainer(
            emptyBehavior = EmptyBehavior.CloseParent,
            filter = acceptFromFlow(),
        )
        LaunchedEffect(viewModel) {
            val key = owner.instruction.navigationKey
            val binding = owner.navigationController.bindingForKeyType(key::class)

            @Suppress("UNCHECKED_CAST")
            binding as ManagedFlowNavigationBinding<NavigationKey, *>

            viewModel.bind(binding.destination(viewModel.getNavigationHandle().asTyped<NavigationKey>()))
            // If the backstack is empty, we manually update the flow to start it, because we don't always want to
            // update the flow when this destination is rendered, because that can cause a completed flow to
            // immediately re-deliver its result.
            if (container.backstack.isEmpty()) viewModel.updateFlow()
        }
        container.Render()
    }
}