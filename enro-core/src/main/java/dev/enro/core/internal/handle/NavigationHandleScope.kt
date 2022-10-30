package dev.enro.core.internal.handle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.factory.ResultChannelFactory
import dev.enro.core.internal.EnroDependencyContainer
import dev.enro.core.internal.EnroDependencyScope
import dev.enro.core.internal.get
import dev.enro.core.internal.register

internal class NavigationHandleScope(
    navigationController: NavigationController,
) : EnroDependencyScope {

    private var boundNavigationHandle: NavigationHandle? = null

    override val container: EnroDependencyContainer = EnroDependencyContainer(
        parentScope = navigationController.dependencyScope,
        registration = {
            register { requireNotNull(boundNavigationHandle) }
            register { ResultChannelFactory(get(), get()) }
        }
    )

    fun bind(navigationHandle: NavigationHandle): NavigationHandleScope {
        boundNavigationHandle = navigationHandle
        navigationHandle.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if(event != Lifecycle.Event.ON_DESTROY) return@LifecycleEventObserver
                boundNavigationHandle = null
            }
        )
        return this
    }
}