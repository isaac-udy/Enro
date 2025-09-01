package dev.enro.test.fixtures

import androidx.compose.runtime.mutableStateOf
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.NavigationContext
import dev.enro.context.RootContext
import dev.enro.test.EnroTest
import dev.enro.ui.NavigationDestination
import kotlin.uuid.Uuid

object NavigationContextFixtures {
    fun createRootContext(): RootContext {
        val owners = TestLifecycleAndViewModelStoreOwner()
        return RootContext(
            id = "TestRootContext",
            parent = Unit,
            controller = EnroTest.getCurrentNavigationController(),
            lifecycleOwner = owners,
            viewModelStoreOwner = owners,
            defaultViewModelProviderFactory = owners,
            activeChildId = mutableStateOf(null)
        )
    }

    fun createContainerContext(
        parent: NavigationContext.WithContainerChildren<*>,
    ): ContainerContext {
        val container = NavigationContainer(
            key = NavigationContainer.Key(Uuid.Companion.random().toString()),
            controller = parent.controller,
        )
        return ContainerContext(
            parent = parent,
            container = container,
        )
    }

    fun <T: NavigationKey> createDestinationContext(
        parent: ContainerContext,
        destination: NavigationDestination<T>,
    ): DestinationContext<T> {
        return DestinationContext(
            parent = parent,
            destination = destination,
            lifecycleOwner = destination.lifecycleOwner,
            viewModelStoreOwner = destination.viewModelStoreOwner,
            defaultViewModelProviderFactory = destination.defaultViewModelProviderFactory,
            activeChildId = mutableStateOf(null),
        )
    }
}