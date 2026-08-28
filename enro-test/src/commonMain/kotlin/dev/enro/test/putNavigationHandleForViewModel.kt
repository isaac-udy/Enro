package dev.enro.test

import androidx.lifecycle.ViewModel
import dev.enro.NavigationKey
import kotlin.reflect.KClass


/**
 * Registers a [TestNavigationHandle] for [key] so that when a [T] ViewModel is constructed,
 * its `by navigationHandle<K>()` delegate (and any `registerForNavigationResult` channels)
 * resolve against this handle.
 *
 * Must be called **before** constructing the ViewModel, because the `navigationHandle`
 * delegate resolves eagerly at ViewModel init time via [NavigationHandleProvider].
 */
inline fun <reified T: ViewModel, K: NavigationKey> putNavigationHandleForViewModel(
    key: K,
) : TestNavigationHandle<K> {
    return putNavigationHandleForViewModel(T::class, key)
}

fun <T: ViewModel, K: NavigationKey> putNavigationHandleForViewModel(
    viewModel: KClass<T>,
    key: K,
) : TestNavigationHandle<K> {
    val testNavigationHandle = createTestNavigationHandle(key)
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    dev.enro.viewmodel.NavigationHandleProvider.put(viewModel, testNavigationHandle)
    return testNavigationHandle
}
