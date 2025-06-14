package dev.enro.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.createSavedStateHandle
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.context.RootContext
import dev.enro.handle.RootNavigationHandle
import dev.enro.handle.getNavigationHandleHolder
import dev.enro.handle.getOrCreateNavigationHandleHolder
import dev.enro.plugin.NavigationPlugin
import dev.enro.ui.destinations.getNavigationKeyInstance
import kotlinx.serialization.Serializable

@PublishedApi
internal object ActivityPlugin : NavigationPlugin() {
    private const val ACTIVE_CONTAINER_KEY = "dev.enro.platform.ACTIVE_CONTAINER_KEY"
    private const val SAVED_INSTANCE_KEY = "dev.enro.platform.SAVED_INSTANCE_KEY"

    private var callbacks: ActivityCallbacks? = null

    override fun onAttached(controller: EnroController) {
        val application = controller.platformReference as? Application ?: return
        if (callbacks != null) {
            application.unregisterActivityLifecycleCallbacks(callbacks)
        }
        callbacks = ActivityCallbacks(controller)
        application.registerActivityLifecycleCallbacks(callbacks)
    }

    override fun onDetached(controller: EnroController) {
        val application = controller.platformReference as? Application ?: return
        application.unregisterActivityLifecycleCallbacks(callbacks)
        callbacks = null
    }

    private class ActivityCallbacks(
        private val controller: EnroController,
    ): Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity !is ComponentActivity) return
            val rootContext = RootContext(
                id = (activity::class.simpleName ?: "UnknownActivity")+"@${activity.hashCode()}",
                parent = activity,
                controller = controller,
                lifecycleOwner = activity,
                viewModelStoreOwner = activity,
                defaultViewModelProviderFactory = activity,
                activeChildId = mutableStateOf(savedInstanceState?.getString(ACTIVE_CONTAINER_KEY))
            )
            val instance = activity.intent.getNavigationKeyInstance()
                ?: savedInstanceState?.getNavigationKeyInstance()
                ?: NavigationKey.Instance(DefaultActivityNavigationKey)

            val navigationHandleHolder = activity.getOrCreateNavigationHandleHolder {
                RootNavigationHandle(
                    instance = instance,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
            val navigationHandle = navigationHandleHolder.navigationHandle
            require(navigationHandle is RootNavigationHandle)
            navigationHandle.bindContext(rootContext)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            if (activity !is ComponentActivity) return
            val navigationHandle = activity.getNavigationHandleHolder().navigationHandle
            require(navigationHandle is RootNavigationHandle) {
                "Expected Activity $activity to have a RootNavigationHandle but was $navigationHandle"
            }
            outState.putString(ACTIVE_CONTAINER_KEY, navigationHandle.context?.activeChild?.id)
            outState.putNavigationKeyInstance(navigationHandle.instance)
        }

        override fun onActivityDestroyed(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
    }
}


@Serializable
internal object DefaultActivityNavigationKey : NavigationKey
