package dev.enro3

import dev.enro3.interceptor.builder.OnNavigationKeyClosedScope
import dev.enro3.interceptor.builder.navigationInterceptor

private typealias OnCloseCallback = OnNavigationKeyClosedScope<*>.() -> Unit
public class NavigationHandleConfiguration<T : NavigationKey>(
    public val navigation: NavigationHandle<T>,
) {
    private val closeables: MutableList<AutoCloseable> = mutableListOf()

    public fun onCloseRequested(
        callback: OnCloseCallback
    ) {
        @Suppress("USELESS_CAST")
        val callbacks = navigation.instance.metadata
            .get(OnCloseCallbacks)
            .plus(callback)

        @Suppress("UNCHECKED_CAST")
        navigation.instance.metadata.set(OnCloseCallbacks, callbacks)
        closeables.add(AutoCloseable {
            val callbacks = navigation.instance.metadata
                .get(OnCloseCallbacks)
                .minus(callback)

            navigation.instance.metadata.set(OnCloseCallbacks, callbacks)
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
        NavigationKey.TransientMetadataKey<List<OnCloseCallback>>(emptyList())

    internal object OnCloseCallbacksEnabled
        : NavigationKey.TransientMetadataKey<Boolean>(true)

    internal companion object {
        internal val onCloseCallbackInterceptor = navigationInterceptor {
            onClosed<NavigationKey> {
                val onCloseRequestedCallbacks = instance.metadata.get(OnCloseCallbacks)
                val enabled = instance.metadata.get(OnCloseCallbacksEnabled)
                if (enabled) {
                    onCloseRequestedCallbacks.forEach {
                        it(this)
                    }
                }
                continueWithClose()
            }
        }
    }
}