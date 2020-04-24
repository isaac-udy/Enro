package nav.enro.core.internal.handle

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import nav.enro.core.internal.addOnBackPressedListener
import nav.enro.core.internal.onEvent
import nav.enro.core.*
import nav.enro.core.context.*
import nav.enro.core.context.ActivityContext
import nav.enro.core.context.FragmentContext
import nav.enro.core.context.NavigationContext
import nav.enro.core.context.leafContext

internal class NavigationHandleViewModel<T : NavigationKey> : ViewModel(), NavigationHandle<T> {

    private var pendingInstruction: NavigationInstruction? = null

    override val key: T get() = navigationContext!!.key

    private var internalOnCloseRequested: () -> Unit = { close() }

    internal var navigationContext: NavigationContext<*, T>? = null
        set(value) {
            field = value
            if (value == null) return

            registerLifecycleObservers(value)
            registerOnBackPressedListener(value)
            executePendingInstruction()
        }


    private fun registerLifecycleObservers(context: NavigationContext<out Any, T>) {
        context.lifecycle.onEvent(Lifecycle.Event.ON_DESTROY) {
            if (context == navigationContext) navigationContext = null
        }
    }

    private fun registerOnBackPressedListener(context: NavigationContext<out Any, T>) {
        if (context is ActivityContext<out FragmentActivity, *>) {
            context.activity.addOnBackPressedListener {
                context.leafContext().navigationHandle().internalOnCloseRequested()
            }
        }
    }

    override fun execute(navigationInstruction: NavigationInstruction) {
        pendingInstruction = navigationInstruction
        executePendingInstruction()
    }

    override fun setOnCloseRequested(onCloseRequested: () -> Unit) {
        internalOnCloseRequested = onCloseRequested
    }

    private fun executePendingInstruction() {
        if(Looper.getMainLooper() != Looper.myLooper()) {
            Handler(Looper.getMainLooper()).post { executePendingInstruction() }
            return
        }
        val context = navigationContext ?: return
        val instruction = pendingInstruction ?: return
        pendingInstruction = null

        when (instruction) {
            NavigationInstruction.Close -> context.controller.close(context.leafContext())
            is NavigationInstruction.Open<*> -> {
                context.controller.open(context, instruction)
            }
        }
    }

    internal fun executeDeeplink() {
        val context = navigationContext ?: TODO("Nice Exception")

        if(context.pendingKeys.isEmpty()) return
        execute(
            NavigationInstruction.Open(
                NavigationDirection.FORWARD,
                context.pendingKeys.first(),
                context.pendingKeys.drop(1)
            )
        )
    }
}

private fun NavigationContext<out Any, *>.navigationHandle(): NavigationHandleViewModel<*> {
    return when (this) {
        is FragmentContext<out Fragment, *> -> fragment.navigationHandle<NavigationKey>().value
        is ActivityContext<out FragmentActivity, *> -> activity.navigationHandle<NavigationKey>().value
    } as NavigationHandleViewModel<*>
}