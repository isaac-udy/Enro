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

/**
 * This is largely copied from the AndroidFragment implementation, but with some changes to support DialogFragments
 */
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
        EnroLog.error("Adding with current state ${fragmentManager.findFragmentByTag(tag)}")
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
                        .runOnCommit {
                            EnroLog.error("Committing $tag")
                        }
                        .add(this, tag)

                    if (fragmentManager.isStateSaved) {
                        // If the state is saved when we add the fragment,
                        // we want to remove the Fragment in onDispose
                        // if isStateSaved never becomes true for the lifetime
                        // of this AndroidFragment - we use a LifecycleObserver
                        // on the Fragment as a proxy for that signal
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
            EnroLog.error("Removing with current state ${fragment}")
            fragmentState.state.value = state
            if (removeEvenIfStateIsSaved) {
                // The Fragment was added when the state was saved and
                // isStateSaved never became true for the lifetime of this
                // AndroidFragment, so we unconditionally remove it here
                fragmentManager.commitNow(allowStateLoss = true) { remove(fragment) }
            } else if (!fragmentManager.isStateSaved) {
                // If the state isn't saved, that means that some state change
                // has removed this Composable from the hierarchy
                fragmentManager.commitNow {
                    remove(fragment)
                }
            }
        }
    }
}