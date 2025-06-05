package dev.enro.test

import androidx.lifecycle.ViewModel
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass


inline fun <reified T: ViewModel> putNavigationHandleForViewModel(
    key: NavigationKey,
) : TestNavigationHandle<NavigationKey> {
    return putNavigationHandleForViewModel(T::class, key)
}

fun <T: ViewModel> putNavigationHandleForViewModel(
    viewModel: KClass<T>,
    key: NavigationKey,
) : TestNavigationHandle<NavigationKey> {
    val testNavigationHandle = createTestNavigationHandle(key)
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    dev.enro.viewmodel.NavigationHandleProvider.put(viewModel, testNavigationHandle)
    return testNavigationHandle
}
