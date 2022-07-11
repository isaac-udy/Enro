package dev.enro.core.container

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import dev.enro.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate

abstract class NavigationContainer(
    val id: String,
    val parentContext: NavigationContext<*>,
    val accept: (NavigationKey) -> Boolean,
    val emptyBehavior: EmptyBehavior
) {
    private val handler = Handler(Looper.getMainLooper())
    private val reconcileBackstack = Runnable {
        reconcileBackstack(pendingRemovals.toList(), mutableBackstack.value)
    }

    abstract val activeContext: NavigationContext<*>?
    abstract val isVisible: Boolean

    private val pendingRemovals = mutableSetOf<AnyOpenInstruction>()
    private val mutableBackstack = MutableStateFlow(createEmptyBackStack())
    val backstackFlow: StateFlow<NavigationContainerBackstack> get() = mutableBackstack

    init {
        parentContext.runWhenContextActive {
            reconcileBackstack.run()
        }
    }

    @MainThread
    fun setBackstack(backstack: NavigationContainerBackstack) = synchronized(this) {
        if(Looper.myLooper() != Looper.getMainLooper()) throw EnroException.NavigationContainerWrongThread(
            "A NavigationContainer's setBackstack method must only be called from the main thread"
        )
        if(backstack == backstackFlow.value) return@synchronized

        handler.removeCallbacks(reconcileBackstack)
        val lastBackstack = mutableBackstack.getAndUpdate { backstack }

        val removed = lastBackstack.backstack
            .filter {
                !backstack.backstack.contains(it)
            }

        val exiting = lastBackstack.backstack
            .firstOrNull {
                it == backstack.exiting
            }

        if(!backstack.isDirectUpdate) {
            if (exiting != null && backstack.lastInstruction is NavigationInstruction.Close) {
                parentContext.containerManager.setActiveContainerById(
                    exiting.internal.previouslyActiveId
                )
            } else {
                parentContext.containerManager.setActiveContainer(this)
            }
        }

        if(backstackFlow.value.backstack.isEmpty()) {
            if(isActive && !backstack.isDirectUpdate) parentContext.containerManager.setActiveContainer(null)
            when(val emptyBehavior = emptyBehavior) {
                EmptyBehavior.AllowEmpty -> {
                    /* If allow empty, pass through to default behavior */
                }
                EmptyBehavior.CloseParent -> {
                    if(parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        parentContext.getNavigationHandle().close()
                    }
                    return
                }
                is EmptyBehavior.Action -> {
                    val consumed = emptyBehavior.onEmpty()
                    if (consumed) {
                        return
                    }
                }
            }
        }

        pendingRemovals.addAll(removed)
        val reconciledBackstack = reconcileBackstack(pendingRemovals.toList(), mutableBackstack.value)
        if(!reconciledBackstack) {
            pendingRemovals.addAll(removed)
            handler.post(reconcileBackstack)
        }
        else {
            pendingRemovals.clear()
        }
    }

    // Returns true if the backstack was able to be reconciled successfully
    abstract fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstack: NavigationContainerBackstack
    ): Boolean

    companion object {
        internal const val PRESENTATION_CONTAINER_LAYOUT_ID = -1
        internal const val PRESENTATION_CONTAINER = PRESENTATION_CONTAINER_LAYOUT_ID.toString()
    }
}

val NavigationContainer.isActive: Boolean
    get() = parentContext.containerManager.activeContainer == this

fun NavigationContainer.setActive() {
    parentContext.containerManager.setActiveContainer(this)
}
