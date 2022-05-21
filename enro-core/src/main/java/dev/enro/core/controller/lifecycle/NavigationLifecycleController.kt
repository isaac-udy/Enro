package dev.enro.core.controller.lifecycle

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.*
import dev.enro.core.controller.container.ExecutorContainer
import dev.enro.core.controller.container.PluginContainer
import dev.enro.core.internal.NoNavigationKey
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.internal.handle.createNavigationHandleViewModel
import java.lang.ref.WeakReference
import java.util.*

internal const val CONTEXT_ID_ARG = "dev.enro.core.ContextController.CONTEXT_ID"

internal class NavigationLifecycleController(
    private val executorContainer: ExecutorContainer,
    private val pluginContainer: PluginContainer
) {
    private val callbacks = NavigationContextLifecycleCallbacks(this)

    fun install(application: Application) {
        callbacks.install(application)
    }

    internal fun uninstall(application: Application) {
        callbacks.uninstall(application)
    }

    fun onContextCreated(context: NavigationContext<*>, savedInstanceState: Bundle?): NavigationHandleViewModel {
        if (context is ActivityContext) {
            context.activity.theme.applyStyle(android.R.style.Animation_Activity, false)
        }

        val instruction = context.arguments.readOpenInstruction()
        val contextId = instruction?.internal?.instructionId
            ?: savedInstanceState?.getString(CONTEXT_ID_ARG)
            ?: UUID.randomUUID().toString()

        val config = NavigationHandleProperty.getPendingConfig(context)
        val defaultKey = config?.defaultKey
            ?: NoNavigationKey(context.contextReference::class.java, context.arguments)
        val defaultInstruction = NavigationInstruction
            .Open.OpenInternal(
                navigationKey = defaultKey,
                navigationDirection = when(defaultKey) {
                    is NavigationKey.SupportsPresent -> NavigationDirection.Present
                    is NavigationKey.SupportsPush -> NavigationDirection.Push
                    else -> NavigationDirection.Present
                }
            )
            .internal
            .copy(instructionId = contextId)

        val viewModelStoreOwner = context.contextReference as ViewModelStoreOwner
        val handle = viewModelStoreOwner.createNavigationHandleViewModel(
            context.controller,
            instruction ?: defaultInstruction
        )

        handle.navigationContext = context
        config?.applyTo(context, handle)
        context.containerManager.restore(savedInstanceState)

        handle.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (!handle.hasKey) return
                if (event == Lifecycle.Event.ON_CREATE) pluginContainer.onOpened(handle)
                if (event == Lifecycle.Event.ON_DESTROY) pluginContainer.onClosed(handle)

                handle.navigationContext?.let {
                    updateActiveNavigationContext(it)
                }
            }
        })
        if (savedInstanceState == null) {
            context.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_START) {
//                       TODO handle.childContainers.forEach { it.openRoot(handle) }
                        handle.executeDeeplink()

                        executorContainer.executorForClose(context).postOpened(context)
                        context.lifecycle.removeObserver(this)
                    }
                }
            })
        }
        return handle
    }

    fun onContextSaved(context: NavigationContext<*>, outState: Bundle) {
        outState.putString(CONTEXT_ID_ARG, context.getNavigationHandleViewModel().id)
        context.containerManager.save(outState)
    }

    private fun updateActiveNavigationContext(context: NavigationContext<*>) {
        // Sometimes the context will be in an invalid state to correctly update, and will throw,
        // in which case, we just ignore the exception
        runCatching {
            activeNavigationHandle = WeakReference(context.rootContext().leafContext().getNavigationHandleViewModel())
        }
    }

    private var activeNavigationHandle: WeakReference<NavigationHandle> = WeakReference(null)
        set(value) {
            if (value.get() == field.get()) return
            field = value

            val active = value.get()
            if (active != null) {
                if (active is NavigationHandleViewModel && !active.hasKey) {
                    field = WeakReference(null)
                    return
                }
                pluginContainer.onActive(active)
            }
        }
}
