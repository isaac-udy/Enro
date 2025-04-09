package dev.enro.core.result

import android.view.View
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import dev.enro.core.NavigationKey
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass


@Suppress("UnusedReceiverParameter") // provided to ensure the method is executed on the correct object
public inline fun <reified T : Any> ComponentActivity.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<T, NavigationKey.WithResult<T>>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, NavigationKey.WithResult<T>>.(T) -> Unit
): PropertyDelegateProvider<ComponentActivity, ReadOnlyProperty<ComponentActivity, NavigationResultChannel<T, NavigationKey.WithResult<T>>>> {
    return createResultChannelProperty(
        onClosed = onClosed,
        onResult = onResult,
    )
}

@Suppress("UnusedReceiverParameter") // provided to ensure the method is executed on the correct object
public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> ComponentActivity.registerForNavigationResult(
    @Suppress("UNUSED_PARAMETER") // provided to allow better type inference
    key: KClass<Key>,
    noinline onClosed: NavigationResultScope<T, Key>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, Key>.(T) -> Unit
): PropertyDelegateProvider<ComponentActivity, ReadOnlyProperty<ComponentActivity, NavigationResultChannel<T, Key>>> {
    return createResultChannelProperty(
        onClosed = onClosed,
        onResult = onResult,
    )
}

@Suppress("UnusedReceiverParameter") // provided to ensure the method is executed on the correct object
public inline fun <reified T : Any> Fragment.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<T, NavigationKey.WithResult<T>>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, NavigationKey.WithResult<T>>.(T) -> Unit
): PropertyDelegateProvider<Fragment, ReadOnlyProperty<Fragment, NavigationResultChannel<T, NavigationKey.WithResult<T>>>> {
    return createResultChannelProperty(
        onClosed = onClosed,
        onResult = onResult,
    )
}

@Suppress("UnusedReceiverParameter") // provided to ensure the method is executed on the correct object
public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> Fragment.registerForNavigationResult(
    @Suppress("UNUSED_PARAMETER") // provided to allow better type inference
    key: KClass<Key>,
    noinline onClosed: NavigationResultScope<T, Key>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, Key>.(T) -> Unit
): PropertyDelegateProvider<Fragment, ReadOnlyProperty<Fragment, NavigationResultChannel<T, Key>>> {
    return createResultChannelProperty(
        onClosed = onClosed,
        onResult = onResult,
    )
}


/**
 * Sets up an UnmanagedEnroResultChannel to be managed by a View.
 *
 * The result channel will be attached when the View is attached to a Window,
 * detached when the view is detached from a Window, and destroyed when the ViewTreeLifecycleOwner
 * lifecycle receives the ON_DESTROY event.
 */
public fun <T : Any, R : NavigationKey.WithResult<T>> UnmanagedNavigationResultChannel<T, R>.managedByView(view: View): NavigationResultChannel<T, R> {
    var activeLifecycle: Lifecycle? = null
    val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) destroy()
    }

    if (view.isAttachedToWindow) {
        attach()
        val lifecycleOwner = view.findViewTreeLifecycleOwner() ?: throw IllegalStateException()
        activeLifecycle = lifecycleOwner.lifecycle.apply {
            addObserver(lifecycleObserver)
        }
    }

    view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            activeLifecycle?.removeObserver(lifecycleObserver)

            attach()
            val lifecycleOwner = view.findViewTreeLifecycleOwner() ?: throw IllegalStateException()
            activeLifecycle = lifecycleOwner.lifecycle.apply {
                addObserver(lifecycleObserver)
            }
        }

        override fun onViewDetachedFromWindow(v: View) {
            detach()
        }
    })
    return this
}

/**
 * Sets up an UnmanagedEnroResultChannel to be managed by a ViewHolder's itemView.
 *
 * The result channel will be attached when the ViewHolder's itemView is attached to a Window,
 * and destroyed when the ViewHolder's itemView is detached from a Window.
 *
 * It is important to understand that this management strategy is appropriate to be called when a
 * ViewHolder is bound to a particular item from the RecyclerView Adapter, not in the constructor of the
 * ViewHolder. When RecyclerView items are recycled, they are first detached from the Window and then re-bound,
 * and then re-attached to the Window. This management strategy will cause the result channel to be
 * destroyed every time the ViewHolder is re-bound to data through onBindViewHolder, which means the
 * result channel should be created each time the ViewHolder is bound.
 */
public fun <T : Any, R : NavigationKey.WithResult<T>> UnmanagedNavigationResultChannel<T, R>.managedByViewHolderItem(
    viewHolder: RecyclerView.ViewHolder
): NavigationResultChannel<T, R> {
    if (viewHolder.itemView.isAttachedToWindow) {
        attach()
    }

    viewHolder.itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            attach()
        }

        override fun onViewDetachedFromWindow(v: View) {
            destroy()
            viewHolder.itemView.removeOnAttachStateChangeListener(this)
        }
    })
    return this
}
