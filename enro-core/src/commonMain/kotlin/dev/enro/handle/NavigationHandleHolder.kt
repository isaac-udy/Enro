package dev.enro.handle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.platform.EnroLog

@PublishedApi
internal class NavigationHandleHolder<T : NavigationKey>(
    instance: NavigationKey.Instance<T>
) : ViewModel() {
    @PublishedApi
    internal var navigationHandle: NavigationHandle<T> by mutableStateOf(NavigationHandleImpl(instance))

    override fun onCleared() {
        val impl = navigationHandle as? NavigationHandleImpl
        impl?.onDestroy()

        navigationHandle = ClearedNavigationHandle(
            instance = navigationHandle.instance
        )
    }

    private class ClearedNavigationHandle<T: NavigationKey>(
        override val instance: NavigationKey.Instance<T>
    ) : NavigationHandle<T>() {
        override val lifecycle: Lifecycle = object : Lifecycle() {
            override val currentState: State = State.DESTROYED
            override fun addObserver(observer: LifecycleObserver) {}
            override fun removeObserver(observer: LifecycleObserver) {}
        }

        override fun execute(
            operation: NavigationOperation,
        ) {
            EnroLog.warn("NavigationHandle with instance $instance has been cleared, but has received an operation which will be ignored")
        }
    }
}
