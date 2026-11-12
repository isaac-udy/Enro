package dev.enro.ui.decorators

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.asBackstack
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.handle.DestinationNavigationHandle
import dev.enro.handle.getOrCreateNavigationHandleHolder
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.LocalNavigationHandle

/**
 * Returns a decorator that creates the [NavigationContext] and the navigation handle for each
 * destination, registers the [NavigationContext] as a child of the container's context, and
 * provides the [NavigationContext] through [LocalNavigationContext].
 *
 * Apply [rememberLifecycleDecorator] and [rememberViewModelStoreDecorator] before this
 * decorator. The decorator throws when [LocalNavigationContext] is not a container's context,
 * or when [LocalViewModelStoreOwner] provides no owner that implements
 * [HasDefaultViewModelProviderFactory].
 */
@Composable
public fun rememberNavigationContextDecorator(): NavigationDestinationDecorator<NavigationKey> = remember {
    navigationContextDecorator()
}

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

        val activeContainerId = rememberSaveable { mutableStateOf<String?>(null) }
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
        val navigationHandle = remember(context) {
            val holder = context.getOrCreateNavigationHandleHolder {
                DestinationNavigationHandle(
                    instance = destination.instance,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
            val navigationHandle = holder.navigationHandle
            require(navigationHandle is DestinationNavigationHandle)
            return@remember navigationHandle
        }
        navigationHandle.bindContext(context)

        DisposableEffect(parentContext, context) {
            parentContext.registerChild(context)
            parentContext.registerVisibility(context, true)
            onDispose {
                parentContext.registerVisibility(context, false)
                parentContext.unregisterChild(context)
            }
        }

        val isActiveInRoot = context.isActiveInRoot
        val isFirstOpen = rememberSaveable { mutableStateOf(true) }

        CompositionLocalProvider(
            LocalNavigationContext provides context,
            LocalNavigationHandle provides navigationHandle,
        ) {
            val isOpened = remember (isFirstOpen.value) {
                if (isFirstOpen.value) {
                    context.controller.plugins.onOpened(navigationHandle)
                }
                isFirstOpen.value = false
                return@remember true
            }
            if (isOpened) {
                destination.Content()
            }
            // This effect must stay after destination.Content(): effects run in composition
            // order, so the content's containers are registered before attachContext runs the
            // operations queued on the navigation handle.
            DisposableEffect(context) {
                navigationHandle.attachContext(context)
                onDispose {}
            }
            DisposableEffect(isActiveInRoot) {
                if (isActiveInRoot) {
                    context.controller.plugins.onActive(navigationHandle)
                }
                onDispose {}
            }
        }

        // TODO this appears to work, but probably not ideal
        DisposableEffect(LocalLifecycleOwner.current.lifecycle.currentStateAsState().value == Lifecycle.State.RESUMED) {
            val resultId = destination.instance.metadata.get(NavigationResultChannel.ResultIdKey)
            if (resultId != null && NavigationResultChannel.hasCompletedResultFor(destination.instance)) {
                context.parent.container.setBackstackDirect(
                    context.parent.container.backstack.filter {
                        it.metadata.get(NavigationResultChannel.ResultIdKey) != resultId
                    }.asBackstack()
                )
            }
            onDispose {  }
        }
    }
}
