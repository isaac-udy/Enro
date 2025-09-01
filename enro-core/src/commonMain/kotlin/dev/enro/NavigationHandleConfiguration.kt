package dev.enro

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.interceptor.builder.OnNavigationKeyClosedScope
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.platform.EnroLog

private typealias OnCloseCallback<T> = NavigationHandle<T>.() -> Unit

public class NavigationHandleConfiguration<T : NavigationKey>(
    private val navigation: NavigationHandle<T>,
) {
    private val closeables: MutableList<AutoCloseable> = mutableListOf()

    // TODO: Add documentation here
    public fun onCloseRequested(
        callback: OnCloseCallback<T>,
    ) {
        @Suppress("USELESS_CAST")
        val callbacks = navigation.instance.metadata
            .get(OnCloseCallbacks)
            .plus(callback)

        @Suppress("UNCHECKED_CAST")
        navigation.instance.metadata.set(OnCloseCallbacks, callbacks as List<OnCloseCallback<*>>)
        closeables.add(AutoCloseable {
            val callbacks = navigation.instance.metadata
                .get(OnCloseCallbacks)
                .minus(callback)

            @Suppress("UNCHECKED_CAST")
            navigation.instance.metadata.set(OnCloseCallbacks, callbacks as List<OnCloseCallback<*>>)
        })
    }

    /**
     * A NavigationHandleConfiguration can be applied inside ViewModels or Composables, where
     * the configuration block may need to be removed/disposed before the NavigationHandle is closed,
     * so we need a way to close/undo the configuration and remove anything that might cause memory leaks
     */
    @PublishedApi
    internal fun close() {
        closeables.forEach { it.close() }
    }

    internal object OnCloseCallbacks :
        NavigationKey.TransientMetadataKey<List<OnCloseCallback<*>>>(emptyList())

    internal companion object {
        internal fun <T : NavigationKey> onCloseRequested(
            navigation: NavigationHandle<T>
        ) {
            val callbacks = navigation.instance.metadata.get(OnCloseCallbacks) as List<OnCloseCallback<T>>
            if (callbacks.isEmpty()) {
                navigation.execute(NavigationOperation.Close(navigation.instance))
            } else {
                val callback = callbacks.last()
                if (callbacks.size > 1) {
                    EnroLog.warn("Multiple onCloseRequested callbacks have been registered for NavigationHandle with key ${navigation.key}, the last registered callback will be used.")
                }
                callback.invoke(navigation)
            }
        }
    }
}
