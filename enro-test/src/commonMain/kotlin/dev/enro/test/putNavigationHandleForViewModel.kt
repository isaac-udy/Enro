package dev.enro.test

import androidx.lifecycle.ViewModel
import dev.enro.NavigationKey
import kotlin.reflect.KClass


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
