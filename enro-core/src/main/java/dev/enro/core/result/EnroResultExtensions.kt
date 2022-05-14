package dev.enro.core.result

import android.view.View
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import dev.enro.core.*
import dev.enro.core.result.internal.LazyResultChannelProperty
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.result.internal.ResultChannelImpl
import dev.enro.core.synthetic.SyntheticDestination
import java.lang.IllegalStateException
import kotlin.properties.ReadOnlyProperty

fun <T : Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.closeWithResult(result: T) {
    val resultId = ResultChannelImpl.getResultId(this)
    if (resultId != null) {
        EnroResult.from(controller).addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = result::class,
                result = result
            )
        )
    }
    close()
}

fun <T : Any> ExecutorArgs<out Any, out Any, out NavigationKey>.sendResult(
    result: T
) {
    val resultId = ResultChannelImpl.getResultId(instruction)
    if (resultId != null) {
        EnroResult.from(fromContext.controller).addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = result::class,
                result = result
            )
        )
    }
}

fun <T : Any> SyntheticDestination<out NavigationKey.WithResult<T>>.sendResult(
    result: T
) {
    val resultId = ResultChannelImpl.getResultId(instruction)
    if (resultId != null) {
        EnroResult.from(navigationContext.controller).addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = result::class,
                result = result
            )
        )
    }
}

fun <T : Any> SyntheticDestination<out NavigationKey.WithResult<T>>.forwardResult(
    navigationKey: NavigationKey.WithResult<T>
) {
    val resultId = ResultChannelImpl.getResultId(instruction)

    // If the incoming instruction does not have a resultId attached, we
    // still want to open the screen we are being forwarded to
    if (resultId == null) {
        navigationContext.getNavigationHandle().executeInstruction(
            NavigationInstruction.DefaultDirection(navigationKey)
        )
    } else {
        navigationContext.getNavigationHandle().executeInstruction(
            ResultChannelImpl.overrideResultId(
                NavigationInstruction.DefaultDirection(navigationKey), resultId
            )
        )
    }
}

inline fun <reified T : Any> ViewModel.registerForNavigationResult(
    navigationHandle: NavigationHandle,
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Any, EnroResultChannel<T>> =
    LazyResultChannelProperty(
        owner = navigationHandle,
        resultType = T::class.java,
        onResult = onResult
    )

inline fun <reified T : Any> ComponentActivity.registerForNavigationResult(
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<ComponentActivity, EnroResultChannel<T>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class.java,
        onResult = onResult
    )

inline fun <reified T : Any> Fragment.registerForNavigationResult(
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Fragment, EnroResultChannel<T>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class.java,
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
inline fun <reified T : Any> NavigationHandle.registerForNavigationResult(
    id: String,
    noinline onResult: (T) -> Unit
): UnmanagedEnroResultChannel<T> {
    return ResultChannelImpl(
        navigationHandle = this,
        resultType = T::class.java,
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
fun <T> UnmanagedEnroResultChannel<T>.managedByLifecycle(lifecycle: Lifecycle): EnroResultChannel<T> {
    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if(event == Lifecycle.Event.ON_START) attach()
        if(event == Lifecycle.Event.ON_STOP) detach()
        if(event == Lifecycle.Event.ON_DESTROY) destroy()
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
fun <T> UnmanagedEnroResultChannel<T>.managedByView(view: View): EnroResultChannel<T> {
    var activeLifecycle: Lifecycle? = null
    val lifecycleObserver = LifecycleEventObserver { _, event ->
        if(event == Lifecycle.Event.ON_DESTROY) destroy()
    }

    if(view.isAttachedToWindow) {
        attach()
        val lifecycleOwner = view.findViewTreeLifecycleOwner() ?: throw IllegalStateException()
        activeLifecycle = lifecycleOwner.lifecycle.apply {
            addObserver(lifecycleObserver)
        }
    }

    view.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            activeLifecycle?.removeObserver(lifecycleObserver)

            attach()
            val lifecycleOwner = view.findViewTreeLifecycleOwner() ?: throw IllegalStateException()
            activeLifecycle = lifecycleOwner.lifecycle.apply {
                addObserver(lifecycleObserver)
            }
        }

        override fun onViewDetachedFromWindow(v: View?) {
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
fun <T> UnmanagedEnroResultChannel<T>.managedByViewHolderItem(viewHolder: RecyclerView.ViewHolder): EnroResultChannel<T> {
    if(viewHolder.itemView.isAttachedToWindow) {
        attach()
    }

    viewHolder.itemView.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            attach()
        }

        override fun onViewDetachedFromWindow(v: View?) {
            destroy()
            viewHolder.itemView.removeOnAttachStateChangeListener(this)
        }
    })
    return this
}