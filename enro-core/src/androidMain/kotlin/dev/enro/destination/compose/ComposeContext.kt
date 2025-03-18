package dev.enro.destination.compose

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.OPEN_ARG
import dev.enro.core.activity
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.destination.activity
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerBackEvent
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.navigationController
import dev.enro.core.isActive
import dev.enro.core.parentContainer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal fun <ContextType : ComposableDestination> ComposeContext(
    contextReference: ContextType,
): NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.owner.activity.application.navigationController },
        getParentContext = { contextReference.owner.parentContainer.context },
        getArguments = { bundleOf(OPEN_ARG to contextReference.owner.instruction) },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
        onBoundToNavigationHandle = {
            bindBackHandling(this, it)
        }
    )
}

private fun bindBackHandling(
    navigationContext: NavigationContext<out ComposableDestination>,
    navigationHandle: NavigationHandle
) {
    val backConfiguration = navigationContext.controller.config.backConfiguration

    when (backConfiguration) {
        is EnroBackConfiguration.Default -> {
            // Should be handled at the Activity level
        }

        is EnroBackConfiguration.Manual -> {
            // Do nothing
        }

        is EnroBackConfiguration.Predictive -> configurePredictiveBackHandling(
            navigationContext,
            navigationHandle
        )
    }
}

private fun configurePredictiveBackHandling(
    navigationContext: NavigationContext<out ComposableDestination>,
    navigationHandle: NavigationHandle
) {
    val activity = navigationContext.activity

    val callback = object : OnBackPressedCallback(false) {
        private var parentContainer: NavigationContainer? = null

        override fun handleOnBackStarted(backEvent: BackEventCompat) {
            parentContainer = navigationContext.parentContainer()
            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Started(navigationContext))
        }

        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Progressed(navigationContext, backEvent))
        }

        override fun handleOnBackPressed() {
            if (parentContainer == null) {
                parentContainer = navigationContext.parentContainer()
            }
            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Confirmed(navigationContext))
            parentContainer = null
        }

        override fun handleOnBackCancelled() {
            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Cancelled(navigationContext))
            parentContainer = null
        }
    }
    activity.onBackPressedDispatcher.addCallback(navigationContext.lifecycleOwner, callback)
    navigationContext.isActive
        .onEach { isActive ->
            callback.isEnabled = isActive
        }
        .launchIn(navigationContext.lifecycleOwner.lifecycleScope)
}