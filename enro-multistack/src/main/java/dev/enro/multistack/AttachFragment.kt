package dev.enro.multistack

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass

internal const val MULTISTACK_CONTROLLER_TAG = "dev.enro.multistack.MULTISTACK_CONTROLLER_TAG"

@PublishedApi
internal class AttachFragment<T : FragmentActivity>(
    private val type: KClass<T>,
    private val fragment: Fragment
) : Application.ActivityLifecycleCallbacks {
    @Suppress("UNCHECKED_CAST")
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        if (type.java.isAssignableFrom(activity::class.java)) {
            activity as T
            activity.supportFragmentManager.beginTransaction()
                .add(fragment, MULTISTACK_CONTROLLER_TAG)
                .commitNow()
            activity.application.unregisterActivityLifecycleCallbacks(this)
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}
