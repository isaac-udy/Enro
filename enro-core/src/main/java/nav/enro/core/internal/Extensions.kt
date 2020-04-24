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