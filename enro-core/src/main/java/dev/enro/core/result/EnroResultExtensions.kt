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
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.synthetic.SyntheticDestination
import dev.enro.extensions.getParcelableCompat
import dev.enro.viewmodel.getNavigationHandle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import dev.enro.core.closeWithResult as nonDeprecatedCloseWithResult

@Deprecated(
    message = "Please use closeWithResult from dev.enro.core",
    level = DeprecationLevel.WARNING,
    replaceWith =
        ReplaceWith("closeWithResult(result)", "dev.enro.core.closeWithResult"),
)
public fun <T : Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.closeWithResult(result: T) {
    nonDeprecatedCloseWithResult(result)
}

public fun <T : Any> ExecutorArgs<out Any, out Any, out NavigationKey.WithResult<T>>.sendResult(
    result: T
) {
    val resultId = instruction.internal.resultId
    if (resultId != null) {
        val keyForResult = instruction.additionalData.getParcelableCompat(PendingResult.OVERRIDE_NAVIGATION_KEY_EXTRA)
            ?: instruction.navigationKey
        if (keyForResult !is NavigationKey.WithResult<*>) return

        EnroResult.from(fromContext.controller).addPendingResult(
            PendingResult.Result(
                resultChannelId = resultId,
                navigationKey = keyForResult,
                resultType = result::class,
                result = result
            )
        )
    }
}

public fun <T : Any> SyntheticDestination<out NavigationKey.WithResult<T>>.sendResult(
    result: T
) {
    val resultId = instruction.internal.resultId
    if (resultId != null) {
        val keyForResult = instruction.additionalData.getParcelableCompat(PendingResult.OVERRIDE_NAVIGATION_KEY_EXTRA)
            ?: instruction.navigationKey
        if (keyForResult !is NavigationKey.WithResult<*>) return

        EnroResult.from(navigationContext.controller).addPendingResult(
            PendingResult.Result(
                resultChannelId = resultId,
                navigationKey = keyForResult,
                resultType = result::class,
                result = result
            )
        )
    }
}

public fun <T : Any> SyntheticDestination<out NavigationKey.WithResult<T>>.forwardResult(
    navigationKey: NavigationKey.WithResult<T>
) {
    val resultId = instruction.internal.resultId

    // If the incoming instruction does not have a resultId attached, we
    // still want to open the screen we are being forwarded to
    if (resultId == null) {
        navigationContext.getNavigationHandle().executeInstruction(
            NavigationInstruction.DefaultDirection(navigationKey)
                .apply {
                    additionalData.putParcelable(PendingResult.OVERRIDE_NAVIGATION_KEY_EXTRA, key)
                }
        )
    } else {
        navigationContext.getNavigationHandle().executeInstruction(
            NavigationInstruction.DefaultDirection(navigationKey)
                .internal
                .copy(
                    resultId = resultId
                )
                .apply {
                    additionalData.putParcelable(PendingResult.OVERRIDE_NAVIGATION_KEY_EXTRA, key)
                }
        )
    }
}

@Deprecated("It is no longer required to provide a navigationHandle")
public inline fun <reified T : Any> ViewModel.registerForNavigationResult(
    navigationHandle: NavigationHandle,
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Any, EnroResultChannel<T, NavigationKey.WithResult<T>>> =
    LazyResultChannelProperty(
        owner = navigationHandle,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any> ViewModel.registerForNavigationResult(
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Any, EnroResultChannel<T, NavigationKey.WithResult<T>>> =
    LazyResultChannelProperty(
        owner = getNavigationHandle(),
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any> ViewModel.registerForNavigationResultWithKey(
    noinline onClosed: (NavigationKey.WithResult<T>) -> Unit = {},
    noinline onResult: (NavigationKey.WithResult<T>, T) -> Unit
): ReadOnlyProperty<Any, EnroResultChannel<T, NavigationKey.WithResult<T>>> =
    LazyResultChannelProperty(
        owner = getNavigationHandle(),
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> ViewModel.registerForNavigationResult(
    key: KClass<Key>,
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Any, EnroResultChannel<T, Key>> =
    LazyResultChannelProperty(
        owner = getNavigationHandle(),
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> ViewModel.registerForNavigationResultWithKey(
    key: KClass<Key>,
    noinline onClosed: (Key) -> Unit = {},
    noinline onResult: (Key, T) -> Unit
): ReadOnlyProperty<Any, EnroResultChannel<T, Key>> =
    LazyResultChannelProperty(
        owner = getNavigationHandle(),
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any> ComponentActivity.registerForNavigationResult(
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<ComponentActivity, EnroResultChannel<T, NavigationKey.WithResult<T>>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any> ComponentActivity.registerForNavigationResultWithKey(
    noinline onClosed: (NavigationKey.WithResult<T>) -> Unit = {},
    noinline onResult: (NavigationKey.WithResult<T>, T) -> Unit
): ReadOnlyProperty<ComponentActivity, EnroResultChannel<T, NavigationKey.WithResult<T>>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> ComponentActivity.registerForNavigationResult(
    key: KClass<Key>,
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<ComponentActivity, EnroResultChannel<T, Key>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> ComponentActivity.registerForNavigationResultWithKey(
    key: KClass<Key>,
    noinline onClosed: (Key) -> Unit = {},
    noinline onResult: (Key, T) -> Unit
): ReadOnlyProperty<ComponentActivity, EnroResultChannel<T, Key>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any> Fragment.registerForNavigationResult(
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Fragment, EnroResultChannel<T, NavigationKey.WithResult<T>>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any> Fragment.registerForNavigationResultWithKey(
    noinline onClosed: (NavigationKey.WithResult<T>) -> Unit = {},
    noinline onResult: (NavigationKey.WithResult<T>, T) -> Unit
): ReadOnlyProperty<Fragment, EnroResultChannel<T, NavigationKey.WithResult<T>>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> Fragment.registerForNavigationResult(
    key: KClass<Key>,
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Fragment, EnroResultChannel<T, Key>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> Fragment.registerForNavigationResultWithKey(
    key: KClass<Key>,
    noinline onClosed: (Key) -> Unit = {},
    noinline onResult: (Key, T) -> Unit
): ReadOnlyProperty<Fragment, EnroResultChannel<T, Key>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult
    )

/**
 * Register for an UnmanagedEnroResultChannel.
 *
 * Be aware that you need to manage the attach/detach/destroy lifecycle events of this result channel
 * yourself, including the initial attach.
 *
 * @see UnmanagedEnroResultChannel
 * @see managedByLifecycle
 * @see managedByView
 */
public inline fun <reified T : Any> NavigationHandle.registerForNavigationResult(
    id: String,
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): UnmanagedEnroResultChannel<T, NavigationKey.WithResult<T>> {
    return createResultChannel(
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult,
        additionalResultId = id
    )
}

/**
 * Register for an UnmanagedEnroResultChannel.
 *
 * Be aware that you need to manage the attach/detach/destroy lifecycle events of this result channel
 * yourself, including the initial attach.
 *
 * @see UnmanagedEnroResultChannel
 * @see managedByLifecycle
 * @see managedByView
 */
public inline fun <reified T : Any> NavigationHandle.registerForNavigationResultWithKey(
    id: String,
    noinline onClosed: (NavigationKey.WithResult<T>) -> Unit = {},
    noinline onResult: (NavigationKey.WithResult<T>, T) -> Unit
): UnmanagedEnroResultChannel<T, NavigationKey.WithResult<T>> {
    return createResultChannel(
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult,
        additionalResultId = id
    )
}

/**
 * Register for an UnmanagedEnroResultChannel.
 *
 * Be aware that you need to manage the attach/detach/destroy lifecycle events of this result channel
 * yourself, including the initial attach.
 *
 * @see UnmanagedEnroResultChannel
 * @see managedByLifecycle
 * @see managedByView
 */
public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> NavigationHandle.registerForNavigationResult(
    id: String,
    key: KClass<Key>,
    noinline onClosed: () -> Unit = {},
    noinline onResult: (T) -> Unit
): UnmanagedEnroResultChannel<T, Key> {
    return createResultChannel(
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult,
        additionalResultId = id
    )
}

/**
 * Register for an UnmanagedEnroResultChannel.
 *
 * Be aware that you need to manage the attach/detach/destroy lifecycle events of this result channel
 * yourself, including the initial attach.
 *
 * @see UnmanagedEnroResultChannel
 * @see managedByLifecycle
 * @see managedByView
 */
public inline fun <reified T : Any, Key : NavigationKey.WithResult<T>> NavigationHandle.registerForNavigationResultWithKey(
    id: String,
    key: KClass<Key>,
    noinline onClosed: (Key) -> Unit = {},
    noinline onResult: (Key, T) -> Unit
): UnmanagedEnroResultChannel<T, Key> {
    return createResultChannel(
        resultType = T::class,
        onClosed = onClosed,
        onResult = onResult,
        additionalResultId = id
    )
}

/**
 * Sets up an UnmanagedEnroResultChannel to be managed by a Lifecycle.
 *
 * The result channel will be attached when the ON_START event occurs, detached when the ON_STOP
 * event occurs, and destroyed when ON_DESTROY occurs.
 */
public fun <T: Any, R : NavigationKey.WithResult<T>> UnmanagedEnroResultChannel<T, R>.managedByLifecycle(
    lifecycle: Lifecycle
): EnroResultChannel<T, R> {
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
public fun <T: Any, R : NavigationKey.WithResult<T>> UnmanagedEnroResultChannel<T, R>.managedByView(view: View): EnroResultChannel<T, R> {
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

    view.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
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
public fun <T: Any, R : NavigationKey.WithResult<T>> UnmanagedEnroResultChannel<T, R>.managedByViewHolderItem(
    viewHolder: RecyclerView.ViewHolder
): EnroResultChannel<T, R> {
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