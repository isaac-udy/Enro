package nav.enro.core.internal

import android.app.Activity
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

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

fun Activity.getAttributeResourceId(attr: Int) = TypedValue().let {
    theme.resolveAttribute(attr, it, true)
    it.resourceId
}

internal val Activity.openEnterAnimation
    get() = TypedValue().let {
        theme.resolveAttribute(android.R.attr.activityOpenEnterAnimation, it, true)
        it.resourceId
    }

internal val Activity.openExitAnimation
    get() = TypedValue().let {
        theme.resolveAttribute(android.R.attr.activityOpenExitAnimation, it, true)
        it.resourceId
    }


internal val Activity.closeEnterAnimation
    get() = TypedValue().let {
        theme.resolveAttribute(android.R.attr.activityCloseEnterAnimation, it, true)
        it.resourceId
    }


internal val Activity.closeExitAnimation
    get() = TypedValue().let {
        theme.resolveAttribute(android.R.attr.activityCloseExitAnimation, it, true)
        it.resourceId
    }