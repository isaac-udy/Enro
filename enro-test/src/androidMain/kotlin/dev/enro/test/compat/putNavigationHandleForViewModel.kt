package dev.enro.test.extensions

import androidx.lifecycle.ViewModel
import dev.enro.NavigationKey
import dev.enro.test.TestNavigationHandle
import dev.enro.test.putNavigationHandleForViewModel as realPutNavigationHandleForViewModel
import kotlin.reflect.KClass

@Deprecated("Use dev.enro.test.putNavigationHandleForViewModel")
inline fun <reified T: ViewModel, K: NavigationKey> putNavigationHandleForViewModel(
    key: K,
) : TestNavigationHandle<K> {
    return realPutNavigationHandleForViewModel(T::class, key)
}

@Deprecated("Use dev.enro.test.putNavigationHandleForViewModel")
fun <T: ViewModel, K: NavigationKey> putNavigationHandleForViewModel(
    viewModel: KClass<T>,
    key: K,
) : TestNavigationHandle<K> {
    return realPutNavigationHandleForViewModel(viewModel, key)
}