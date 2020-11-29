package nav.enro.core.internal

import android.content.res.Resources
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import nav.enro.core.context.*
import nav.enro.core.getNavigationHandle
import nav.enro.core.internal.handle.NavigationHandleViewModel

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

internal fun NavigationContext<out Any>.navigationHandle(): NavigationHandleViewModel {
    return when (this) {
        is FragmentContext<out Fragment> -> fragment.getNavigationHandle()
        is ActivityContext<out FragmentActivity> -> activity.getNavigationHandle()
    } as NavigationHandleViewModel
}

internal fun Resources.Theme.getAttributeResourceId(attr: Int) = TypedValue().let {
    resolveAttribute(attr, it, true)
    it.resourceId
}