package dev.enro.test.application

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.activeChildContext
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationContext
import kotlin.reflect.KClass

fun ComposeTestRule.waitForNavigationHandle(
    block: (NavigationHandle) -> Boolean
): NavigationHandle {
    var navigationHandle: NavigationHandle? = null
    waitUntil(5_000) {
        val activity = runOnUiThread {
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .singleOrNull() as? ComponentActivity
        } ?: return@waitUntil false

        var activeContext: NavigationContext<*>? = activity.navigationContext
        while (activeContext != null) {
            navigationHandle = runCatching { activeContext!!.getNavigationHandle() }.getOrNull()
                ?: return@waitUntil false
            if (block(navigationHandle!!)) {
                return@waitUntil true
            }
            activeContext = activeContext.activeChildContext()
        }
        false
    }
    return navigationHandle!!
}

inline fun <reified T: Fragment> ComposeTestRule.waitForFragment(
    noinline block: (T) -> Boolean = { true }
): T {
    var fragment: T? = null
    waitUntil {
        runOnUiThread {
            val activities = ActivityLifecycleMonitorRegistry
                .getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<FragmentActivity>()

            fragment = activities.firstNotNullOfOrNull {
                it.supportFragmentManager.getFragment(T::class, block)
            }

            fragment != null
        }
    }
    return fragment!!
}

@Suppress("UNCHECKED_CAST")
fun <T: Fragment> FragmentManager.getFragment(
    type: KClass<T>,
    block: (T) -> Boolean,
): T? {
    val found = fragments.firstOrNull {
        type.java.isAssignableFrom(it::class.java) && block(it as T)
    } as? T
    if (found != null) return found
    return fragments.firstNotNullOfOrNull {
        it.childFragmentManager.getFragment(type, block)
    }
}
