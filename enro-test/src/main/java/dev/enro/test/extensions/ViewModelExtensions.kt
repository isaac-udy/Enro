package dev.enro.test.extensions

import androidx.lifecycle.ViewModel
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.test.TestNavigationHandle
import dev.enro.test.createTestNavigationHandle
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
    val providerClass = Class.forName("dev.enro.viewmodel.EnroViewModelNavigationHandleProvider")
    val instance = providerClass.getDeclaredField("INSTANCE").get(null)
    val putMethod = providerClass.getDeclaredMethod("put", java.lang.Class::class.java, NavigationHandle::class.java)
    val mockedNavigationHandle = createTestNavigationHandle<NavigationKey>(key)
    putMethod.invoke(instance, viewModel.java, mockedNavigationHandle)
    return mockedNavigationHandle
}