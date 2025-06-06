package dev.enro.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.enro.EnroController
import dev.enro.context.RootContext
import dev.enro.plugin.NavigationPlugin

// TODO do we want to be adding this as a ViewModel or just grabbing it dynamically?
internal object ActivityPlugin : NavigationPlugin() {

    private const val ACTIVE_CONTAINER_KEY = "dev.enro.platform.ACTIVE_CONTAINER_KEY"

    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity !is ComponentActivity) return
            activity.viewModels<RootContextHolder>().value.rootContext = RootContext(
                lifecycleOwner = activity,
                viewModelStoreOwner = activity,
                defaultViewModelProviderFactory = activity,
                activeChildId = mutableStateOf(savedInstanceState?.getString(ACTIVE_CONTAINER_KEY))
            )
        }
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            if (activity !is ComponentActivity) return
            val context = activity.viewModels<RootContextHolder>().value.rootContext ?: return
            outState.putString(ACTIVE_CONTAINER_KEY, context.activeChild?.id)
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity !is ComponentActivity) return
            activity.viewModels<RootContextHolder>().value.rootContext = null
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
    }

    override fun onAttached(controller: EnroController) {
        val application = controller.platformReference as? Application ?: return
        application.registerActivityLifecycleCallbacks(callbacks)
    }

    override fun onDetached(controller: EnroController) {
        val application = controller.platformReference as? Application ?: return
        application.unregisterActivityLifecycleCallbacks(callbacks)
    }
}

internal class RootContextHolder : ViewModel() {
    internal var rootContext: RootContext? = null
    override fun onCleared() {
        rootContext = null
    }
}

public val Activity.navigationContext: RootContext
    get() {
        if (this !is ComponentActivity) {
            error("Cannot retrieve navigation context from Activity that does not extend ComponentActivity")
        }
        return viewModels<RootContextHolder>().value.rootContext
            ?: error("Navigation context is not available for this activity")
    }