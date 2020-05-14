package nav.enro.core.internal

import android.app.Activity
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import nav.enro.core.NavigationKey
import nav.enro.core.context.*
import nav.enro.core.context.ActivityContext
import nav.enro.core.context.FragmentContext
import nav.enro.core.getNavigationHandle
import nav.enro.core.internal.handle.NavigationHandleViewModel
import nav.enro.core.navigationHandle

internal fun Lifecycle.onEvent(on: Lifecycle.Event, block: () -> Unit) {
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if(on == event) {
                block()
            }
        }
    })
}

internal fun FragmentActivity.addOnBackPressedListener(block: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            block()
        }
    })
}

internal fun NavigationContext<out Any, *>.navigationHandle(): NavigationHandleViewModel<*> {
    return when (this) {
        is FragmentContext<out Fragment, *> -> fragment.getNavigationHandle()
        is ActivityContext<out FragmentActivity, *> -> activity.getNavigationHandle<NavigationKey>()
    } as NavigationHandleViewModel<*>
}