package dev.enro.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.context.RootContext
import dev.enro.plugin.NavigationPlugin
import dev.enro.ui.destinations.getNavigationKeyInstance
import kotlinx.serialization.Serializable

@PublishedApi
internal object ActivityPlugin : NavigationPlugin() {
    private const val ACTIVE_CONTAINER_KEY = "dev.enro.platform.ACTIVE_CONTAINER_KEY"
    private const val SAVED_INSTANCE_KEY = "dev.enro.platform.SAVED_INSTANCE_KEY"

    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity !is ComponentActivity) return
            activity.activityContextHolder.rootContext = RootContext(
                parent = activity,
                lifecycleOwner = activity,
                viewModelStoreOwner = activity,
                defaultViewModelProviderFactory = activity,
                activeChildId = mutableStateOf(savedInstanceState?.getString(ACTIVE_CONTAINER_KEY))
            )
            val instance = activity.intent.getNavigationKeyInstance()
                ?: savedInstanceState?.getNavigationKeyInstance()
                ?: NavigationKey.Instance(DefaultActivityNavigationKey)

            activity.activityContextHolder.navigationHandle = ActivityNavigationHandle(
                activity = activity,
                instance = instance,
            )
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            if (activity !is ComponentActivity) return
            val context = activity.activityContextHolder.rootContext
            if (context != null) {
                outState.putString(ACTIVE_CONTAINER_KEY, context.activeChild?.id)
            }
            val instance = activity.activityContextHolder.navigationHandle?.instance
            if (instance != null) {
                outState.putNavigationKeyInstance(instance)
            }
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity !is ComponentActivity) return
            activity.activityContextHolder.rootContext = null
            activity.activityContextHolder.navigationHandle = null
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


@Serializable
internal object DefaultActivityNavigationKey : NavigationKey
