package dev.enro

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.interceptor.builder.OnNavigationKeyClosedScope
import dev.enro.interceptor.builder.navigationInterceptor

private typealias OnCloseCallback<T> = NavigationHandle<T>.() -> Unit

/**
 * Receiver of the `NavigationHandle.configure { … }` block in a Composable
 * and of the `config` block of the `ViewModel.navigationHandle` delegate.
 *
 * The registrations made on a [NavigationHandleConfiguration] are removed:
 * - when the Composable that called `configure` leaves the composition
 * - when the ViewModel that declared the delegate is cleared
 */
public class NavigationHandleConfiguration<T : NavigationKey>(
    private val navigation: NavigationHandle<T>,
) {
    private val closeables: MutableList<AutoCloseable> = mutableListOf()

    /**
     * Registers [callback] to run in place of the close when [requestClose]
     * is called. The destination closes only if [callback] calls [close].
     *
     * A [NavigationHandle] supports one registered callback. With two
     * callbacks registered, [requestClose] throws [IllegalStateException].
     * Register the callback in one place for each destination: the
     * ViewModel, or the top-level Composable.
     */
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

    // Removes each registration. The ViewModel or Composable that applied the configuration
    // can be cleared or disposed before the NavigationHandle is closed.
    @PublishedApi
    internal fun close() {
        closeables.forEach { it.close() }
    }

    internal object OnCloseCallbacks :
        NavigationKey.TransientMetadataKey<List<OnCloseCallback<*>>>(
            default = emptyList(),
            name = "dev.enro.NavigationHandleConfiguration.OnCloseCallbacks",
        )

    internal companion object {
        internal fun <T : NavigationKey> onCloseRequested(
            navigation: NavigationHandle<T>
        ) {
            val callbacks = navigation.instance.metadata.get(OnCloseCallbacks) as List<OnCloseCallback<T>>
            when {
                callbacks.isEmpty() -> {
                    navigation.execute(NavigationOperation.Close(navigation.instance))
                }
                callbacks.size > 1 -> {
                    error(
                        "Multiple onCloseRequested callbacks have been registered for NavigationHandle " +
                            "with key ${navigation.key}. Only one onCloseRequested callback may be active " +
                            "for a given NavigationHandle at a time — register it in exactly one place " +
                            "(typically the destination's ViewModel, otherwise the top-level Composable)."
                    )
                }
                else -> {
                    callbacks.single().invoke(navigation)
                }
            }
        }
    }
}
