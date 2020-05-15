package nav.enro.core

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import nav.enro.core.context.navigationContext
import java.lang.IllegalStateException
import kotlin.concurrent.thread

inline fun <reified T: NavigationKey> ActivityScenario<out FragmentActivity>.getNavigationHandle(): NavigationHandle<T> {
    var result: NavigationHandle<T>? = null
    onActivity{
        result = it.getNavigationHandle<T>()
    }

    val handle = result ?: throw IllegalStateException("Could not retrieve NavigationHandle from Activity")
    val key = handle.key as? T
        ?: throw IllegalStateException("Handle was of incorrect type. Expected ${T::class.java.name} but was ${handle.key::class.java.name}")
    return handle
}

inline fun <reified T: FragmentActivity> expectActivity(crossinline selector: (FragmentActivity) -> Boolean = { it is T }): T {
    return waitOnMain {
        val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
        val activity = activities.firstOrNull()

        return@waitOnMain when {
            activity !is FragmentActivity -> null
            selector(activity) -> activity as T
            else -> null
        }
    }
}


internal inline fun <reified T: Fragment> expectFragment(crossinline selector: (Fragment) -> Boolean = { it is T }): T {
    val activity = expectActivity<FragmentActivity>()
    return waitOnMain {
        val fragment = activity.supportFragmentManager.primaryNavigationFragment ?: return@waitOnMain null
        if(selector(fragment)) return@waitOnMain fragment as T else null
    }
}

fun expectNoActivity() {
    waitOnMain {
        val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.PRE_ON_CREATE).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.CREATED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.STARTED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.PAUSED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.STOPPED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESTARTED).toList()
        return@waitOnMain if(activities.isEmpty()) true else null
    }
}

fun waitFor(block: () -> Boolean) {
    val maximumTime = 10_000
    val startTime = System.currentTimeMillis()
    while(true) {
        if(block()) return
        Thread.sleep(100)
        if(System.currentTimeMillis() - startTime > maximumTime) throw IllegalStateException("Took too long waiting")
    }
}

fun <T: Any> waitOnMain(block: () -> T?): T {
    val maximumTime = 10_000
    val startTime = System.currentTimeMillis()
    var currentResponse: T? = null

    while(true) {
        if (System.currentTimeMillis() - startTime > maximumTime) throw IllegalStateException("Took too long waiting")
        Thread.sleep(100)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            currentResponse = block()
        }
        currentResponse?.let { return it }
    }
}

