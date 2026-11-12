package dev.enro.ui.destinations.fragment

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.fragment.compose.FragmentState
import androidx.fragment.compose.rememberFragmentState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.platform.EnroLog

// A copy of the androidx AndroidFragment composable, changed to add a DialogFragment.
@Composable
internal fun <T : DialogFragment> AndroidDialogFragment(
    clazz: Class<T>,
    tag: String,
    fragmentState: FragmentState = rememberFragmentState(),
    arguments: Bundle = Bundle.EMPTY,
    onUpdate: (T) -> Unit = { },
) {
    val updateCallback = rememberUpdatedState(onUpdate)
    val view = LocalView.current
    val fragmentManager = remember(view) {
        FragmentManager.findFragmentManager(view)
    }
    val tag = currentCompositeKeyHash.toString()
    val context = LocalContext.current
    DisposableEffect(fragmentManager, clazz, fragmentState) {
        var removeEvenIfStateIsSaved = false
        val fragment = fragmentManager.findFragmentByTag(tag)
            ?: fragmentManager.fragmentFactory
                .instantiate(context.classLoader, clazz.name)
                .apply {
                    this as DialogFragment
                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    setInitialSavedState(fragmentState.state.value)
                    setArguments(arguments)
                    val transaction = fragmentManager
                        .beginTransaction()
                        .add(this, tag)

                    if (fragmentManager.isStateSaved) {
                        // The fragment is added while the FragmentManager state is saved.
                        // onDispose removes the fragment with state loss, unless the fragment
                        // was started before onDispose.
                        removeEvenIfStateIsSaved = true
                        lifecycle.addObserver(
                            object : DefaultLifecycleObserver {
                                override fun onStart(owner: LifecycleOwner) {
                                    removeEvenIfStateIsSaved = false
                                    lifecycle.removeObserver(this)
                                }
                            }
                        )
                        transaction.commitNowAllowingStateLoss()
                    } else {
                        transaction.commitNow()
                    }
                }
        @Suppress("UNCHECKED_CAST") updateCallback.value(fragment as T)
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        onDispose {
            val state = fragmentManager.saveFragmentInstanceState(fragment)
            fragmentState.state.value = state
            if (removeEvenIfStateIsSaved) {
                fragmentManager.commitNow(allowStateLoss = true) { remove(fragment) }
            } else if (!fragmentManager.isStateSaved) {
                // With the state not saved, a state change removed this Composable from the
                // composition, so the fragment is removed.
                fragmentManager.commitNow {
                    remove(fragment)
                }
            }
        }
    }
}