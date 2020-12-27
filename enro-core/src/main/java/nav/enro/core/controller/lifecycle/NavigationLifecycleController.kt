package nav.enro.core.controller.lifecycle

import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import nav.enro.core.*
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.NavigationController
import nav.enro.core.controller.container.ExecutorContainer
import nav.enro.core.controller.container.PluginContainer
import nav.enro.core.controller.navigationController
import nav.enro.core.internal.NoNavigationKey
import nav.enro.core.internal.handle.NavigationHandleViewModel
import nav.enro.core.internal.handle.createNavigationHandleViewModel
import java.lang.IllegalStateException
import java.util.*

private const val CONTEXT_ID_ARG = "nav.enro.core.ContextController.CONTEXT_ID"

internal class NavigationLifecycleController(
    private val executorContainer: ExecutorContainer,
    private val pluginContainer: PluginContainer
) {

    private val callbacks = NavigationContextLifecycleCallbacks(this)

    fun install(application: Application) {
        application as? NavigationApplication
            ?: throw IllegalStateException("Application MUST be a NavigationApplication")

        callbacks.install(application)
    }

    fun onContextCreated(context: NavigationContext<*>, savedInstanceState: Bundle?) {
        if(context is ActivityContext) {
            context.activity.theme.applyStyle(android.R.style.Animation_Activity, false)
            context.activity.findViewById<ViewGroup>(android.R.id.content).viewTreeObserver.addOnGlobalLayoutListener {
                activeNavigationHandle = context.activity.navigationContext.leafContext().getNavigationHandleViewModel()
            }
        }

        val instruction = context.arguments.readOpenInstruction()
        val contextId = instruction?.instructionId
            ?: savedInstanceState?.getString(CONTEXT_ID_ARG)
            ?: UUID.randomUUID().toString()

        val config = NavigationHandleProperty.getPendingConfig(context)
        val defaultInstruction = NavigationInstruction.Open(
            instructionId = contextId,
            navigationDirection = NavigationDirection.FORWARD,
            navigationKey = config?.defaultKey ?: NoNavigationKey(context.contextReference::class.java, context.arguments)
        )
        val viewModelStoreOwner = context.contextReference as ViewModelStoreOwner
        val handle = viewModelStoreOwner.createNavigationHandleViewModel(
            context.controller,
            instruction ?: defaultInstruction
        )
        config?.applyTo(handle)
        handle.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(!handle.hasKey) return
                if (event == Lifecycle.Event.ON_CREATE) pluginContainer.onOpened(handle)
                if (event == Lifecycle.Event.ON_DESTROY) pluginContainer.onClosed(handle)
            }
        })
        handle.navigationContext = context
        if(savedInstanceState == null) {
            executorContainer.executorForClose(context).postOpened(context)
        }
        if(savedInstanceState == null) handle.executeDeeplink()
    }

    fun onContextSaved(context: NavigationContext<*>, outState: Bundle) {
        outState.putString(CONTEXT_ID_ARG, context.getNavigationHandleViewModel().id)
    }

    fun onContextResumed(context: NavigationContext<*>) {
        activeNavigationHandle = context.leafContext().getNavigationHandleViewModel()
    }

    private var activeNavigationHandle: NavigationHandle? = null
        set(value) {
            if(value == field) return
            field = value
            if(value != null) {
                if(value is NavigationHandleViewModel && !value.hasKey) {
                    field = null
                    return
                }
                pluginContainer.onActive(value)
            }
        }
}
