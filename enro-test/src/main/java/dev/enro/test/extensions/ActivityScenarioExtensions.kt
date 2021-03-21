package dev.enro.test.extensions

import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.getNavigationHandle
import dev.enro.test.TestNavigationHandle

fun <T : NavigationKey> ActivityScenario<out FragmentActivity>.getTestNavigationHandle(type: Class<T>): TestNavigationHandle<T> {
    var result: NavigationHandle? = null
    onActivity {
        result = it.getNavigationHandle()
    }

    val handle = result
        ?: throw IllegalStateException("Could not retrieve NavigationHandle from Activity")

    if (!type.isAssignableFrom(handle.key::class.java)) {
        throw IllegalStateException("Handle was of incorrect type. Expected ${type.name} but was ${handle.key::class.java.name}")
    }
    return TestNavigationHandle(handle)
}

inline fun <reified T : NavigationKey> ActivityScenario<out FragmentActivity>.getTestNavigationHandle(): TestNavigationHandle<T> =
    getTestNavigationHandle(T::class.java)