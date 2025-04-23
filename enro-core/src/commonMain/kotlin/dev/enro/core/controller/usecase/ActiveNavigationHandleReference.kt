package dev.enro.core.controller.usecase

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.repository.PluginRepository
import dev.enro.core.getNavigationHandle
import dev.enro.core.internal.EnroWeakReference
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.internal.hasKey
import dev.enro.core.leafContext
import dev.enro.core.rootContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ActiveNavigationHandleReference(
    private val pluginRepository: PluginRepository
) {
    private var activeNavigationHandle: EnroWeakReference<NavigationHandle> = EnroWeakReference(null)
        set(value) {
            if (value.get() == field.get()) { return }
            field = value

            val active = value.get()
            if (active != null) {
                if (active is NavigationHandleViewModel && !active.hasKey) {
                    field = EnroWeakReference(null)
                    mutableActiveNavigationIdFlow.value = null
                    return
                }
                mutableActiveNavigationIdFlow.value = active.id
                pluginRepository.onActive(active)
            } else {
                mutableActiveNavigationIdFlow.value = null
            }
        }

    private val mutableActiveNavigationIdFlow = MutableStateFlow<String?>(null)
    val activeNavigationIdFlow = mutableActiveNavigationIdFlow

    private fun updateActiveNavigationContext(context: NavigationContext<*>) {
        // Sometimes the context will be in an invalid state to correctly update, and will throw,
        // in which case, we just ignore the exception
        runCatching {
            val active = context.rootContext().leafContext().getNavigationHandle()
            if (!active.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return@runCatching
            activeNavigationHandle = EnroWeakReference(active)
        }
    }

    fun watchActiveNavigationHandleFrom(
        context: NavigationContext<*>,
        navigationHandle: NavigationHandleViewModel,
    ) {
        navigationHandle.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (!navigationHandle.hasKey) return
                if (event == Lifecycle.Event.ON_CREATE) pluginRepository.onOpened(navigationHandle)
                if (event == Lifecycle.Event.ON_DESTROY) pluginRepository.onClosed(navigationHandle)

                navigationHandle.navigationContext?.let {
                    updateActiveNavigationContext(it)
                }
            }
        })

        context.containerManager.activeContainerFlow
            .onEach {
                val activeContainerContext = navigationHandle.navigationContext ?: return@onEach
                if (activeContainerContext.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    updateActiveNavigationContext(activeContainerContext)
                }
            }
            .launchIn(context.lifecycle.coroutineScope)
    }
}