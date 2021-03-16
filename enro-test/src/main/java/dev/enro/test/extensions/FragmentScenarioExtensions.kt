package dev.enro.test.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.getNavigationHandle
import dev.enro.test.TestNavigationHandle

fun <T : NavigationKey> FragmentScenario<*>.getTestNavigationHandle(type: Class<T>): TestNavigationHandle<T> {
    @Suppress("UNCHECKED_CAST")
    this as FragmentScenario<Fragment>

    var result: NavigationHandle? = null
    onFragment {
        result = it.getNavigationHandle()
    }

    val handle = result
        ?: throw IllegalStateException("Could not retrieve NavigationHandle from Fragment")

    if (!type.isAssignableFrom(handle.key::class.java)) {
        throw IllegalStateException("Handle was of incorrect type. Expected ${type.name} but was ${handle.key::class.java.name}")
    }
    return TestNavigationHandle(handle)
}

inline fun <reified T : NavigationKey> FragmentScenario<*>.getTestNavigationHandle(): TestNavigationHandle<T> =
    getTestNavigationHandle(T::class.java)