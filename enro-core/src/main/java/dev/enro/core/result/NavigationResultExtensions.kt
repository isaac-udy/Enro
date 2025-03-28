package dev.enro.core.result

import android.view.View
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import dev.enro.core.*
import dev.enro.core.controller.usecase.createResultChannel
import dev.enro.core.result.internal.LazyResultChannelProperty
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

public fun <T : Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.deliverResultFromPush(
    navigationKey: NavigationKey.SupportsPush.WithResult<out T>
) {
    executeInstruction(
        AdvancedResultExtensions.getInstructionToForwardResult(
            originalInstruction = instruction,
            direction = NavigationDirection.Push,
            navigationKey = navigationKey,
        )
    )
}

public fun <T : Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.deliverResultFromPresent(
    navigationKey: NavigationKey.SupportsPresent.WithResult<out T>
) {
    executeInstruction(
        AdvancedResultExtensions.getInstructionToForwardResult(
            originalInstruction = instruction,
            direction = NavigationDirection.Present,
            navigationKey = navigationKey,
        )
    )
}

@Suppress("UnusedReceiverParameter") // provided to ensure the method is executed on the correct object
public inline fun <reified T : Any> ViewModel.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<T, NavigationKey.WithResult<T>>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, NavigationKey.WithResult<T>>.(T) -> Unit
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationResultChannel<T, NavigationKey.WithResult<T>>>> {
    return createResultChannelProperty(
        onClosed = onClosed,
        onResult = onResult,
    )
}

@Suppress("UnusedReceiverParameter") // provided to ensure the method is executed on the correct object
public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> ViewModel.registerForNavigationResult(
    @Suppress("UNUSED_PARAMETER") // provided to allow better type inference
    key: KClass<Key>,
    noinline onClosed: NavigationResultScope<T, Key>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, Key>.(T) -> Unit
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationResultChannel<T, Key>>> {
    return createResultChannelProperty(
        onClosed = onClosed,
        onResult = onResult,
    )
}

@JvmName("registerForNavigationResultDelegated")
public inline fun <reified T : Any> registerForNavigationResult(
    owner: ViewModel,
    additionalResultId: String = "",
    noinline onClosed: NavigationResultScope<T, NavigationKey.WithResult<T>>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, NavigationKey.WithResult<T>>.(T) -> Unit
): PropertyDelegateProvider<Any, ReadOnlyProperty<Any, NavigationResultChannel<T, NavigationKey.WithResult<T>>>> {
    return createResultChannelProperty(
        owner = owner,
        additionalResultId = additionalResultId,
        onClosed = onClosed,
        onResult = onResult,
    )
}

@JvmName("registerForNavigationResultDelegated")
public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> registerForNavigationResult(
    @Suppress("UNUSED_PARAMETER") // provided to allow better type inference
    key: KClass<Key>,
    owner: ViewModel,
    additionalResultId: String = "",
    noinline onClosed: NavigationResultScope<T, Key>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, Key>.(T) -> Unit
): PropertyDelegateProvider<Any, ReadOnlyProperty<Any, NavigationResultChannel<T, Key>>> {
    return createResultChannelProperty(
        owner = owner,
        additionalResultId = additionalResultId,
        onClosed = onClosed,
        onResult = onResult,
    )
}

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

@PublishedApi
internal inline fun <Owner : Any, reified Result : Any, Key : NavigationKey.WithResult<Result>> createResultChannelProperty(
    owner: Any? = null,
    additionalResultId: String = "",
    noinline onClosed: NavigationResultScope<Result, Key>.() -> Unit,
    noinline onResult: NavigationResultScope<Result, Key>.(Result) -> Unit,
): PropertyDelegateProvider<Owner, ReadOnlyProperty<Owner, NavigationResultChannel<Result, Key>>> {
    return PropertyDelegateProvider { thisRef, property ->
        val resultId = "${thisRef::class.java.name}.${property.name}"
        LazyResultChannelProperty(
            owner = owner ?: thisRef,
            resultType = Result::class,
            resultId = resultId,
            onClosed = onClosed,
            onResult = onResult,
            additionalResultId = additionalResultId,
        )
    }
}

/**
 * Register for an UnmanagedEnroResultChannel.
 *
 * Be aware that you need to manage the attach/detach/destroy lifecycle events of this result channel
 * yourself, including the initial attach.
 *
 * @see UnmanagedNavigationResultChannel
 * @see managedByLifecycle
 * @see managedByView
 */
public inline fun <reified T : Any> NavigationHandle.registerForNavigationResult(
    id: String,
    noinline onClosed: NavigationResultScope<T, NavigationKey.WithResult<T>>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, NavigationKey.WithResult<T>>.(T) -> Unit
): UnmanagedNavigationResultChannel<T, NavigationKey.WithResult<T>> {
    val resultId = object {}
    return createResultChannel(
        resultType = T::class,
        resultId = resultId::class.java.name,
        onClosed = onClosed,
        onResult = onResult,
        additionalResultId = id,
    )
}

/**
 * Register for an UnmanagedEnroResultChannel.
 *
 * Be aware that you need to manage the attach/detach/destroy lifecycle events of this result channel
 * yourself, including the initial attach.
 *
 * @see UnmanagedNavigationResultChannel
 * @see managedByLifecycle
 * @see managedByView
 */
public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> NavigationHandle.registerForNavigationResult(
    id: String,
    key: KClass<Key>,
    noinline onClosed: NavigationResultScope<T, Key>.() -> Unit = {},
    noinline onResult: NavigationResultScope<T, Key>.(T) -> Unit
): UnmanagedNavigationResultChannel<T, Key> {
    val resultId = object {}
    return createResultChannel(
        resultType = T::class,
        resultId = resultId::class.java.name,
        onClosed = onClosed,
        onResult = onResult,
        additionalResultId = id,
    )
}

/**
 * Sets up an UnmanagedEnroResultChannel to be managed by a Lifecycle.
 *
 * The result channel will be attached when the ON_START event occurs, detached when the ON_STOP
 * event occurs, and destroyed when ON_DESTROY occurs.
 */
public fun <T : Any, R : NavigationKey.WithResult<T>> UnmanagedNavigationResultChannel<T, R>.managedByLifecycle(
    lifecycle: Lifecycle
): NavigationResultChannel<T, R> {
    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) attach()
        if (event == Lifecycle.Event.ON_STOP) detach()
        if (event == Lifecycle.Event.ON_DESTROY) destroy()
    })
    return this
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