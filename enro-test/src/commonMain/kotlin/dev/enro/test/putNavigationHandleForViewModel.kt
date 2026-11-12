package dev.enro.test

import androidx.lifecycle.ViewModel
import dev.enro.NavigationKey
import kotlin.reflect.KClass


/**
 * Creates a [TestNavigationHandle] for [key] and registers the handle for the ViewModel
 * class [T]. The `by navigationHandle<K>()` delegate and each `registerForNavigationResult`
 * channel of a [T] ViewModel use the registered handle.
 *
 * Call putNavigationHandleForViewModel before constructing the ViewModel: the
 * `navigationHandle` delegate reads the registered handle when the ViewModel is constructed.
 */
public inline fun <reified T: ViewModel, K: NavigationKey> putNavigationHandleForViewModel(
    key: K,
) : TestNavigationHandle<K> {
    return putNavigationHandleForViewModel(T::class, key)
}

public fun <T: ViewModel, K: NavigationKey> putNavigationHandleForViewModel(
    viewModel: KClass<T>,
    key: K,
) : TestNavigationHandle<K> {
    val testNavigationHandle = createTestNavigationHandle(key)
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    dev.enro.viewmodel.NavigationHandleProvider.put(viewModel, testNavigationHandle)
    return testNavigationHandle
}
