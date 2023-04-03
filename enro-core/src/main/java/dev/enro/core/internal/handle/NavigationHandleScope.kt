package dev.enro.core.internal.handle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.*
import dev.enro.core.controller.usecase.CreateResultChannel
import dev.enro.core.controller.usecase.NavigationHandleExtras
import java.io.Closeable

internal class NavigationHandleScope(
    navigationController: NavigationController,
) : EnroDependencyScope, Closeable {

    private var boundNavigationHandle: NavigationHandle? = null

    override val container: EnroDependencyContainer = EnroDependencyContainer(
        parentScope = navigationController.dependencyScope,
        registration = {
            register { requireNotNull(boundNavigationHandle) }
            register { CreateResultChannel(get(), get()) }
            register { NavigationHandleExtras() }
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

    override fun close() {
        container.bindings.values.forEach {
            if(it.isInitialized) {
                val closeable = it.value as? Closeable ?: return@forEach
                closeable.close()
            }
        }
    }
}