package dev.enro

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.interceptor.builder.OnNavigationKeyClosedScope
import dev.enro.interceptor.builder.navigationInterceptor

private typealias OnCloseCallback<T> = NavigationHandle<T>.() -> Unit

/**
 * Scope for configuring lifecycle-aware behaviour on a [NavigationHandle].
 *
 * Obtain one of these via `NavigationHandle.configure { … }` inside a
 * Composable or via the `config` lambda on the `ViewModel.navigationHandle`
 * delegated property. The configuration's lifetime is tied to the call
 * site:
 *
 * - In a Composable, `configure` wraps a [androidx.compose.runtime.DisposableEffect],
 *   so the registrations are torn down when the composable leaves
 *   composition.
 * - In a `ViewModel`, the configuration is attached to the ViewModel's
 *   `addCloseable`, so it lives as long as the ViewModel does.
 *
 * Registrations made in this scope (currently only [onCloseRequested]) are
 * automatically removed when the configuration is closed — you can
 * register without worrying about manual cleanup.
 */
public class NavigationHandleConfiguration<T : NavigationKey>(
    private val navigation: NavigationHandle<T>,
) {
    private val closeables: MutableList<AutoCloseable> = mutableListOf()

    /**
     * Registers [callback] to run when something asks the destination to
     * close via [requestClose] (system back gesture, predictive back,
     * "X" affordances calling `requestClose`).
     *
     * Use this to insert pre-close work — a confirmation dialog, a "save
     * draft?" prompt, an unsaved-changes guard. Inside the callback, call
     * [close] yourself when you actually want the destination to close;
     * if you don't, the destination stays.
     *
     * Only one callback may be active per handle at a time — registering a
     * second while the first is still in scope throws at the moment the
     * close is requested. Register in exactly one place per destination
     * (typically the ViewModel; otherwise the top-level Composable).
     *
     * The registration is removed automatically when the surrounding
     * configuration is disposed, so you don't need to unregister manually.
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
