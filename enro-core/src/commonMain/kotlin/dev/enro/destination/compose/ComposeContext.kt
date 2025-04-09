package dev.enro.destination.compose

import androidx.savedstate.savedState
import dev.enro.core.NavigationContext
import dev.enro.core.addOpenInstruction
import dev.enro.core.compose.destination.navigationController
import dev.enro.core.controller.EnroBackConfiguration

internal fun <ContextType : ComposableDestination> ComposeContext(
    contextReference: ContextType,
): NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.owner.navigationController },
        getParentContext = { contextReference.owner.parentContainer.context },
        getArguments = { savedState { addOpenInstruction(contextReference.owner.instruction) } },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
        onBoundToNavigationHandle = {
            bindBackHandling(this, it)
        }
    )
}

internal fun bindBackHandling(
    navigationContext: NavigationContext<out ComposableDestination>,
    navigationHandle: dev.enro.core.NavigationHandle
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
    navigationHandle: dev.enro.core.NavigationHandle
) {
    TODO()
//    val activity = navigationContext.activity
//
//    val callback = object : OnBackPressedCallback(false) {
//        private var parentContainer: NavigationContainer? = null
//
//        override fun handleOnBackStarted(backEvent: BackEventCompat) {
//            parentContainer = navigationContext.parentContainer()
//            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Started(navigationContext))
//        }
//
//        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
//            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Progressed(navigationContext, backEvent))
//        }
//
//        override fun handleOnBackPressed() {
//            if (parentContainer == null) {
//                parentContainer = navigationContext.parentContainer()
//            }
//            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Confirmed(navigationContext))
//            parentContainer = null
//        }
//
//        override fun handleOnBackCancelled() {
//            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Cancelled(navigationContext))
//            parentContainer = null
//        }
//    }
//    activity.onBackPressedDispatcher.addCallback(navigationContext.lifecycleOwner, callback)
//    navigationContext.isActive
//        .onEach { isActive ->
//            callback.isEnabled = isActive
//        }
//        .launchIn(navigationContext.lifecycleOwner.lifecycleScope)
}