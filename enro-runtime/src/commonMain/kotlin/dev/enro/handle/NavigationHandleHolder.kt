package dev.enro.handle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.platform.EnroLog

@PublishedApi
internal class NavigationHandleHolder<T : NavigationKey>(
    navigationHandle: NavigationHandle<T>,
) : ViewModel() {
    @PublishedApi
    internal var navigationHandle: NavigationHandle<T> by mutableStateOf(navigationHandle)
        private set

    override fun onCleared() {
        when (val impl = navigationHandle) {
            is DestinationNavigationHandle -> impl.onDestroy()
            is RootNavigationHandle -> impl.onDestroy()
        }
        navigationHandle = ClearedNavigationHandle(
            instance = navigationHandle.instance
        )
    }

    private class ClearedNavigationHandle<T: NavigationKey>(
        override val instance: NavigationKey.Instance<T>
    ) : NavigationHandle<T>() {
        override val savedStateHandle: SavedStateHandle = SavedStateHandle()

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

@PublishedApi
internal fun <T: NavigationKey> ViewModelStoreOwner.getOrCreateNavigationHandleHolder(
    createNavigationHandle: CreationExtras.() -> NavigationHandle<T>,
): NavigationHandleHolder<T> {
    return ViewModelProvider.create(
        owner = this,
        factory = viewModelFactory {
            addInitializer(NavigationHandleHolder::class) {
                NavigationHandleHolder(createNavigationHandle())
            }
        },
        extras = (this as HasDefaultViewModelProviderFactory).defaultViewModelCreationExtras,
    ).get<NavigationHandleHolder<T>>()
}

@PublishedApi
internal fun ViewModelStoreOwner.getNavigationHandleHolder(): NavigationHandleHolder<*> {
    return ViewModelProvider.create(
        owner = this,
        factory = viewModelFactory {
            addInitializer(NavigationHandleHolder::class) {
                error("Expected NavigationHandleHolder to be present in ViewModelStoreOwner ${this@getNavigationHandleHolder}, but it was missing")
            }
        },
        extras = CreationExtras.Empty,
    ).get<NavigationHandleHolder<*>>()
}
