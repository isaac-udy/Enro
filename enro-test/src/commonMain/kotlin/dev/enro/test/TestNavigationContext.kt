@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.NavigationContext
import dev.enro.context.RootContext
import dev.enro.ui.NavigationDestination

internal fun createRootContext() : RootContext {
    val owners = ContextOwners()
    return RootContext(
        lifecycleOwner = owners,
        viewModelStoreOwner = owners,
        defaultViewModelProviderFactory = owners,
        activeChildId = mutableStateOf(null),
    )
}

internal fun createContainerContext(
    parent: NavigationContext<*, ContainerContext>,
    container: NavigationContainer,
) : ContainerContext {
    require(parent is RootContext || parent is DestinationContext<*>) {}

    return ContainerContext(
        parent = parent,
        container = container,
    ).also {
        parent.registerChild(it)
    }
}

internal fun <T : NavigationKey> createDestinationContext(
    parent: ContainerContext,
    instance: NavigationKey.Instance<T>,
    metadata: Map<String, Any> = emptyMap(),
) : DestinationContext<T> {
    val owners = ContextOwners()
    return DestinationContext(
        lifecycleOwner = owners,
        viewModelStoreOwner = owners,
        defaultViewModelProviderFactory = owners,
        parent = parent,
        destination = NavigationDestination.createWithoutScope(
            instance = instance,
            metadata = metadata,
            content = { error("NavigationDestinations cannot be rendered for tests") }
        ),
        activeChildId = mutableStateOf(null),
    ).also {
        @Suppress("UNCHECKED_CAST")
        parent.registerChild(it as DestinationContext<NavigationKey>)
    }
}

@PublishedApi
internal class ContextOwners() : LifecycleOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {
    override val lifecycle: Lifecycle = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.RESUMED
    }

    override val viewModelStore: ViewModelStore = ViewModelStore()

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = TODO("Not yet implemented")
}