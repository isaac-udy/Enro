package dev.enro.tests.application

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
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
        runCatching {
            it.getNavigationHandle()
                .let(block)
        }
            .getOrNull() == true
    }
    return context.getNavigationHandle()
}

fun ComposeTestRule.waitForNavigationContext(
    block: (NavigationContext<*>) -> Boolean
): NavigationContext<*> {
    var navigationContext: NavigationContext<*>? = null
    waitUntil(5_000) {
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

inline fun <reified T : Fragment> ComposeTestRule.waitForFragment(
    waitForResume: Boolean = true,
    noinline block: (T) -> Boolean = { true }
): T {
    lateinit var fragment: T
    waitUntil {
        runOnUiThread {
            val activities = ActivityLifecycleMonitorRegistry
                .getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<FragmentActivity>()

            fragment = activities.firstNotNullOfOrNull {
                it.supportFragmentManager.getFragment(T::class, block)
            } ?: return@runOnUiThread false

            return@runOnUiThread true
        }
    }
    if (waitForResume) {
        waitUntil {
            fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
    }
    return fragment
}

@Suppress("UNCHECKED_CAST")
fun <T : Fragment> FragmentManager.getFragment(
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

fun ComposeTestRule.waitForText(
    text: String,
    timeoutMillis: Long = 5000
) {
    waitUntil(timeoutMillis) {
        try {
            onNodeWithText(text).assertExists()
            true
        } catch (e: Exception) {
            false
        }
    }
}

fun ComposeTestRule.waitForViewBasedText(
    text: String,
    timeoutMillis: Long = 5000
) {
    waitUntil(timeoutMillis) {
        try {
            Espresso.onView(ViewMatchers.withText(text))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            true
        } catch (e: Exception) {
            false
        }
    }
}
