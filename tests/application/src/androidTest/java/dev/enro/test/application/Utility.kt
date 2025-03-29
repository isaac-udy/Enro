package dev.enro.test.application

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.isRoot
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
    val context = waitForNavigationContext {
        runCatching { it.getNavigationHandle()
            .let(block) }
            .getOrNull() == true
    }
    return context.getNavigationHandle()
}

fun ComposeTestRule.waitForNavigationContext(
    block: (NavigationContext<*>) -> Boolean
): NavigationContext<*> {
    var navigationContext: NavigationContext<*>? = null
    waitUntil(10_000) {
        // fetch the root semantics nodes to cause the Compose test rule to refresh content,
        // as it appears sometimes external changes don't properly cause the content to refresh
        onAllNodes(isRoot()).fetchSemanticsNodes()

        val activity = runOnIdle {
            runOnUiThread {
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                    .singleOrNull() as? ComponentActivity
            }
        } ?: return@waitUntil false

        var activeContext: NavigationContext<*>? = activity.navigationContext
        while (activeContext != null) {
            if (block(activeContext)) {
                navigationContext = activeContext
                return@waitUntil true
            }
            activeContext = activeContext.activeChildContext()
        }
        false
    }
    return navigationContext!!
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
