package dev.enro.core.controller.lifecycle

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import dev.enro.core.*
import dev.enro.core.controller.repository.ExecutorRepository
import dev.enro.core.controller.repository.PluginRepository
import dev.enro.core.controller.usecase.GetNavigationExecutor
import dev.enro.core.controller.usecase.forClosing
import dev.enro.core.internal.NoNavigationKey
import dev.enro.core.internal.get
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.internal.handle.createNavigationHandleViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.ref.WeakReference
import java.util.*

internal const val CONTEXT_ID_ARG = "dev.enro.core.ContextController.CONTEXT_ID"

internal class NavigationLifecycleController(
    private val executorRepository: ExecutorRepository,
    private val pluginRepository: PluginRepository
) {
    private val callbacks = NavigationContextLifecycleCallbacks(this)

    fun install(application: Application) {
        callbacks.install(application)
    }

    internal fun uninstall(application: Application) {
        callbacks.uninstall(application)
    }

    fun onContextCreated(
        context: NavigationContext<*>,
        savedInstanceState: Bundle?
    ): NavigationHandleViewModel {
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
                navigationDirection = when (defaultKey) {
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
                if (event == Lifecycle.Event.ON_CREATE) pluginRepository.onOpened(handle)
                if (event == Lifecycle.Event.ON_DESTROY) pluginRepository.onClosed(handle)

                handle.navigationContext?.let {
                    updateActiveNavigationContext(it)
                }
            }
        })

        context.containerManager.activeContainerFlow
            .onEach {
                val context = handle.navigationContext ?: return@onEach
                if (context.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    updateActiveNavigationContext(context)
                }
            }
            .launchIn(context.lifecycle.coroutineScope)

        if (savedInstanceState == null) {
            handle.runWhenHandleActive { handle.executeDeeplink() }
            context.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_START) {
                        context.controller.dependencyScope.get<GetNavigationExecutor>().forClosing(
                            context
                        ).postOpened(context)
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
            val active = context.rootContext().leafContext().getNavigationHandleViewModel()
            if (!active.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return@runCatching
            activeNavigationHandle = WeakReference(active)
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
                pluginRepository.onActive(active)
            }
        }
}
