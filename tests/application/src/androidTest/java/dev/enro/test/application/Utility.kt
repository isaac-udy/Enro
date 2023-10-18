package dev.enro.test.application

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import dev.enro.core.NavigationHandle
import dev.enro.tests.application.TestApplicationPlugin
import kotlin.reflect.KClass

fun ComposeTestRule.waitForNavigationHandle(
    block: (NavigationHandle) -> Boolean
): NavigationHandle {
    var navigationHandle: NavigationHandle? = null
    waitUntil {
        navigationHandle = TestApplicationPlugin.activeNavigationHandle
        navigationHandle != null && block(navigationHandle!!)
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
