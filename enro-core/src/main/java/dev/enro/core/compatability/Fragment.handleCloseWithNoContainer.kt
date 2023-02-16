package dev.enro.core.compatability

import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import dev.enro.core.NavigationContext
import dev.enro.core.close
import dev.enro.core.getNavigationHandle
import dev.enro.core.parentContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal fun handleCloseWithNoContainer(context: NavigationContext<*>) : Boolean {
    if (context.contextReference !is Fragment) return false

    val container = context.parentContainer()
    if (container != null) return false

    /*
     * There are some cases where a Fragment's FragmentManager can be removed from the Fragment.
     * There is (as far as I am aware) no easy way to check for the FragmentManager being removed from the
     * Fragment, other than attempting to catch the exception that is thrown in the case of a missing
     * parentFragmentManager.
     *
     * If a Fragment's parentFragmentManager has been destroyed or removed, there's very little we can
     * do to resolve the problem, and the most likely case is if
     *
     * The most common case where this can occur is if a DialogFragment is closed in response
     * to a nested Fragment closing with a result - this causes the DialogFragment to close,
     * and then for the nested Fragment to attempt to close immediately afterwards, which fails because
     * the nested Fragment is no longer attached to any fragment manager (and won't be again).
     *
     * see ResultTests.whenResultFlowIsLaunchedInDialogFragment_andCompletesThroughTwoNestedFragments_thenResultIsDelivered
     */
    runCatching {
        context.contextReference.parentFragmentManager
    }
        .onSuccess { fragmentManager ->
            runCatching { fragmentManager.executePendingTransactions() }
                .onFailure {
                    // if we failed to execute pending transactions, we're going to
                    // re-attempt to close this context (by executing "close" on it's NavigationHandle)
                    // but we're going to delay for 1 millisecond first, which will allow the
                    // main thread to finish executing the transaction before attempting the close
                    val navigationHandle = context.contextReference.getNavigationHandle()
                    navigationHandle.lifecycleScope.launch {
                        delay(1)
                        navigationHandle.close()
                    }
                }
                .onSuccess {
                    fragmentManager.commitNow {
                        setReorderingAllowed(true)
                        remove(context.contextReference)
                    }
                }
        }
    return true
}