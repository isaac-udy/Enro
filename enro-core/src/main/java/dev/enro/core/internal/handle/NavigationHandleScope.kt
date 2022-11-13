package dev.enro.core.internal.handle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.enro.core.*
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.factory.ResultChannelFactoryImpl
import dev.enro.core.usecase.ResultChannelFactory

internal class NavigationHandleScope(
    navigationController: NavigationController,
) : EnroDependencyScope {

    private var boundNavigationHandle: NavigationHandle? = null

    override val container: EnroDependencyContainer = EnroDependencyContainer(
        parentScope = navigationController.dependencyScope,
        registration = {
            register { requireNotNull(boundNavigationHandle) }
            register<ResultChannelFactory> { ResultChannelFactoryImpl(get(), get()) }
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