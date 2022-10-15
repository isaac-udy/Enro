package dev.enro.core.result

import dev.enro.core.NavigationKey

public interface EnroResultChannel<Result, Key : NavigationKey.WithResult<Result>> {
    @Deprecated("Please use push or present")
    public fun open(key: Key)
    public fun push(key: NavigationKey.SupportsPush.WithResult<Result>)
    public fun present(key: NavigationKey.SupportsPresent.WithResult<Result>)
}

/**
 * An UnmanagedEnroResultChannel is an EnroResultChannel that does not manage its own lifecycle.
 *
 * An UnmanagedEnroResultChannel will always be destroyed when the NavigationHandle that was used to
 * create it is destroyed (unless the UnmanagedEnroResultChannel has been destroyed before this).
 *
 * An EnroResultChannel is usually tied to the lifecycle of some UI component, such as a Fragment,
 * Activity, or Composable function. When this UI component is not visible, the EnroResultChannel
 * will enter a detached state, which means it will not receive updates until the UI component is
 * visible again. When the UI component becomes visible, it will be attached again and will receive
 * any pending results, as well as being ready to receive any other results that are sent.
 *
 * An UnmanagedEnroResult channel allows you to manage the attach, detach, and destroy events of the
 * EnroResultChannel yourself.
 *
 * This is primarily useful when a component wants to maintain a lifecycle that is shorter than
 * the regular Activity/Fragment/ViewModel lifecycles. For example, in the ViewHolder for a RecyclerView,
 * result channels should be destroyed when their associated View is detached/recycled, otherwise you
 * could end up with thousands of active result channels. Similarly, if a custom View maintains a
 * result channel, it may be useful to tie the UnmanagedEnroResultChannel's attach/detach to the
 * View's onAttachedToWindow/onDetachedFromWindow, so that the View does not receive results while it
 * is not attached to a window.
 *
 * There are extension functions available to manage an UnmanagedEnroResultChannel with a Lifecycle,
 * View, or ViewHolder.
 *
 * @see managedByLifecycle
 * @see managedByView
 * @see managedByViewHolderItem
 */
public interface UnmanagedEnroResultChannel<Result, Key : NavigationKey.WithResult<Result>> :
    EnroResultChannel<Result, Key> {
    public fun attach()
    public fun detach()
    public fun destroy()
}