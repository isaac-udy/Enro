package dev.enro3.handle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import dev.enro3.NavigationContext
import dev.enro3.NavigationHandle
import dev.enro3.NavigationKey
import dev.enro3.NavigationOperation
import dev.enro3.platform.EnroLog

@PublishedApi
internal class NavigationHandleHolder<T : NavigationKey>(
    instance: NavigationKey.Instance<T>
) : ViewModel() {
    @PublishedApi
    internal var navigationHandle: NavigationHandle<T> by mutableStateOf(NavigationHandleImpl(instance))

    fun bindContext(
        context: NavigationContext<T>,
    ) {
        require(context.destination.instance.id == navigationHandle.id) {
            "Cannot bind NavigationContext with instance ${context.destination.instance} to NavigationHandle with instance ${navigationHandle.instance}"
        }
        when(val navigationHandle = navigationHandle) {
            is ClearedNavigationHandle -> error("Attempted to bindContext for NavigationHandle that was cleared")
            is NavigationHandleImpl<T> -> navigationHandle.bindContext(context)
            else -> error("NavigationHandle was of unexpected type ${navigationHandle::class.qualifiedName}")
        }
    }

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
            target: NavigationOperation.Target,
            operation: NavigationOperation,
        ) {
            EnroLog.warn("NavigationHandle with instance $instance has been cleared, but has received an operation which will be ignored")
        }
    }
}
