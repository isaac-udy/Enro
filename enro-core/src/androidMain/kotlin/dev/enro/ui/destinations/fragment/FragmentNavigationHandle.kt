package dev.enro.ui.destinations.fragment

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.platform.EnroLog
import dev.enro.ui.NavigationDestinationScope

@PublishedApi
internal class FragmentNavigationHandle<T : NavigationKey>() : NavigationHandle<T>() {
    internal var delegate: NavigationHandle<T> = NotInitialized()
    override lateinit var instance: NavigationKey.Instance<T>

    @AdvancedEnroApi
    override fun execute(operation: NavigationOperation) {
        delegate.execute(operation)
    }

    override val lifecycle: Lifecycle get() = delegate.lifecycle

    internal fun bind(scope: NavigationDestinationScope<T>) {
        instance = scope.navigation.instance
        delegate = scope.navigation
    }

    internal fun unbind() {
        delegate = NotInitialized()
    }

    internal class NotInitialized<T : NavigationKey>() : NavigationHandle<T>() {
        override lateinit var instance: NavigationKey.Instance<T>

        override val lifecycle: Lifecycle = object : Lifecycle() {
            override val currentState: State = State.INITIALIZED
            override fun addObserver(observer: LifecycleObserver) {}
            override fun removeObserver(observer: LifecycleObserver) {}
        }

        override fun execute(
            operation: NavigationOperation,
        ) {
            EnroLog.warn("NavigationHandle with instance $instance has been not been initialised, but has received an operation which will be ignored")
        }
    }
}