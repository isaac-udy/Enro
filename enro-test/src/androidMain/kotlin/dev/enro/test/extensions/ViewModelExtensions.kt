@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test.extensions

import androidx.lifecycle.ViewModel
import dev.enro.core.NavigationKey
import dev.enro.test.TestNavigationHandle
import dev.enro.test.createTestNavigationHandle
import dev.enro.viewmodel.EnroViewModelNavigationHandleProvider
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
    val mockedNavigationHandle = createTestNavigationHandle(key)
    EnroViewModelNavigationHandleProvider.put(viewModel, mockedNavigationHandle)
    return mockedNavigationHandle
}