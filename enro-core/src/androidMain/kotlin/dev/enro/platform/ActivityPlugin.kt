package dev.enro.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import dev.enro.EnroController
import dev.enro.NavigationContext
import dev.enro.plugin.NavigationPlugin

// TODO do we want to be adding this as a ViewModel or just grabbing it dynamically?
internal object ActivityPlugin : NavigationPlugin() {
    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity !is ComponentActivity) return
            activity.viewModels<RootContextHolder>().value.rootContext = NavigationContext.Root(
                lifecycleOwner = activity,
                viewModelStoreOwner = activity,
                defaultViewModelProviderFactory = activity,
            )
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity !is ComponentActivity) return
            activity.viewModels<RootContextHolder>().value.rootContext = null
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
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
    internal var rootContext: NavigationContext.Root? = null
    override fun onCleared() {
        rootContext = null
    }
}

public val Activity.navigationContext: NavigationContext.Root
    get() {
        if (this !is ComponentActivity) {
            error("Cannot retrieve navigation context from Activity that does not extend ComponentActivity")
        }
        return viewModels<RootContextHolder>().value.rootContext
            ?: error("Navigation context is not available for this activity")
    }