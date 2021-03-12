package dev.enro

import android.app.Application
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import dev.enro.core.*

private val debug = false

inline fun <reified T: NavigationKey> ActivityScenario<out FragmentActivity>.getNavigationHandle(): TypedNavigationHandle<T> {
    var result: NavigationHandle? = null
    onActivity{
        result = it.getNavigationHandle()
    }

    val handle = result ?: throw IllegalStateException("Could not retrieve NavigationHandle from Activity")
    val key = handle.key as? T
        ?: throw IllegalStateException("Handle was of incorrect type. Expected ${T::class.java.name} but was ${handle.key::class.java.name}")
    return handle.asTyped()
}

class TestNavigationContext<Context: Any, KeyType: NavigationKey>(
    val context: Context,
    val navigation: TypedNavigationHandle<KeyType>
)

inline fun <reified ContextType: Any, reified KeyType: NavigationKey> expectContext(
    crossinline selector: (TestNavigationContext<ContextType, KeyType>) -> Boolean = { true }
): TestNavigationContext<ContextType, KeyType> {
    return when {
        Fragment::class.java.isAssignableFrom(ContextType::class.java) -> {
            waitOnMain {
                val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                val activity = activities.firstOrNull() as? FragmentActivity ?: return@waitOnMain null
                var fragment = activity.supportFragmentManager.primaryNavigationFragment

                while(fragment != null) {
                    if (fragment is ContextType) {
                        val context = TestNavigationContext(
                            fragment as ContextType,
                            fragment.getNavigationHandle().asTyped<KeyType>()
                        )
                        if (selector(context)) return@waitOnMain context
                    }
                    fragment = fragment.childFragmentManager.primaryNavigationFragment
                }
                return@waitOnMain null
            }
        }
        FragmentActivity::class.java.isAssignableFrom(ContextType::class.java) -> waitOnMain {
            val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            val activity = activities.firstOrNull()
            if(activity !is FragmentActivity) return@waitOnMain null
            if(activity !is ContextType) return@waitOnMain null

            val context = TestNavigationContext(
                activity as ContextType,
                activity.getNavigationHandle().asTyped<KeyType>()
            )
            return@waitOnMain if(selector(context)) context else null
        }
        else -> throw RuntimeException("Failed to get context type ${ContextType::class.java.name}")
    }
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

internal inline fun <reified T: Fragment> expectNoFragment(crossinline selector: (Fragment) -> Boolean = { it is T }): Boolean {
    val activity = expectActivity<FragmentActivity>()
    return waitOnMain {
        val fragment = activity.supportFragmentManager.primaryNavigationFragment ?: return@waitOnMain true
        if(selector(fragment)) return@waitOnMain null else true
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
    val maximumTime = 7_000
    val startTime = System.currentTimeMillis()

    while(true) {
        if(block()) return
        Thread.sleep(250)
        if(System.currentTimeMillis() - startTime > maximumTime) throw IllegalStateException("Took too long waiting")
    }
}

fun <T: Any> waitOnMain(block: () -> T?): T {
    if(debug) { Thread.sleep(3000) }

    val maximumTime = 7_000
    val startTime = System.currentTimeMillis()
    var currentResponse: T? = null

    while(true) {
        if (System.currentTimeMillis() - startTime > maximumTime) throw IllegalStateException("Took too long waiting")
        Thread.sleep(250)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            currentResponse = block()
        }
        currentResponse?.let { return it }
    }
}

val application: Application get() =
    InstrumentationRegistry.getInstrumentation().context.applicationContext as Application
