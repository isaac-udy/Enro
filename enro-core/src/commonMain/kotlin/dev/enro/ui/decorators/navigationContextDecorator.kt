package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.handle.NavigationHandleHolder
import dev.enro.handle.NavigationHandleImpl
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.LocalNavigationHandle

/**
 * Returns a [NavigationDestinationDecorator] that provides navigation context to destinations.
 *
 * This decorator establishes the navigation context for each destination, including:
 * - Navigation handle binding
 * - Container hierarchy
 * - Access to lifecycle and ViewModelStore owners from parent decorators
 *
 * **Note:** This decorator requires the following decorators to be applied before it:
 * - [navigationLifecycleDecorator] or [rememberLifecycleDecorator] for lifecycle management
 * - [viewModelStoreDecorator] or [rememberViewModelStoreDecorator] for ViewModel support
 */
@Composable
public fun rememberNavigationContextDecorator(): NavigationDestinationDecorator<NavigationKey> = remember {
    navigationContextDecorator()
}

/**
 * Creates a [NavigationDestinationDecorator] that provides navigation context.
 *
 * This decorator creates and binds the [NavigationContext] for each destination,
 * providing access to navigation functionality through composition locals.
 */
internal fun navigationContextDecorator(): NavigationDestinationDecorator<NavigationKey> {
    return navigationDestinationDecorator { destination ->
        val parentContext = LocalNavigationContext.current
        require(parentContext is ContainerContext) {
            "Parent context must be a NavigationContext.Container"
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        val viewModelStoreOwner = LocalViewModelStoreOwner.current

        requireNotNull(viewModelStoreOwner) {
            "No ViewModelStoreOwner available. Ensure ViewModelStoreDecorator is applied before NavigationContextDecorator."
        }
        require(viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
            "ViewModelStoreOwner must implement HasDefaultViewModelProviderFactory"
        }

        // Get or create the NavigationHandleHolder for this destination
        @Suppress("UNCHECKED_CAST")
        val navigationHandleHolder = viewModel<NavigationHandleHolder<NavigationKey>>(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            NavigationHandleHolder(instance = destination.instance as NavigationKey.Instance<NavigationKey>)
        }

        val activeContainerId = rememberSaveable { mutableStateOf<String?>(null) }
        // Create the navigation context for this destination
        val context = remember(parentContext, destination) {
            DestinationContext(
                lifecycleOwner = lifecycleOwner,
                viewModelStoreOwner = viewModelStoreOwner,
                defaultViewModelProviderFactory = viewModelStoreOwner,
                destination = destination,
                activeChildId = activeContainerId,
                parent = parentContext,
            )
        }
        DisposableEffect(parentContext, context) {
            parentContext.registerChild(context)
            parentContext.registerVisibility(context, true)
            onDispose {
                parentContext.registerVisibility(context, false)
                parentContext.unregisterChild(context)
            }
        }

        val navigationHandle = remember(navigationHandleHolder){
            val navigationHandle = navigationHandleHolder.navigationHandle
            require(navigationHandle is NavigationHandleImpl<NavigationKey>)
            navigationHandle.bindContext(context)
            return@remember navigationHandle
        }

        // Provide navigation-specific composition locals
        CompositionLocalProvider(
            LocalNavigationContext provides context,
            LocalNavigationHandle provides navigationHandleHolder.navigationHandle,
        ) {
            destination.content()
        }

        // TODO this appears to work, but probably not ideal
        DisposableEffect(LocalLifecycleOwner.current.lifecycle.currentStateAsState().value == Lifecycle.State.RESUMED) {
            val resultId = destination.instance.metadata.get(NavigationResultChannel.ResultIdKey)
            val pendingResults = NavigationResultChannel.pendingResults.value
            if (resultId != null && pendingResults[resultId] is NavigationResult.Completed<*>) {
                context.parent.container.setBackstackDirect(
                    context.parent.container.backstack.filter {
                        it.metadata.get(NavigationResultChannel.ResultIdKey) != resultId
                    }
                )
            }
            onDispose {  }
        }
    }
}
