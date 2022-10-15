package dev.enro.core.container

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import dev.enro.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate

abstract class NavigationContainer(
    val id: String,
    val parentContext: NavigationContext<*>,
    val emptyBehavior: EmptyBehavior,
    val acceptsNavigationKey: (NavigationKey) -> Boolean,
    val acceptsDirection: (NavigationDirection) -> Boolean,
    val acceptsNavigator: (Navigator<*, *>) -> Boolean
) {
    private val handler = Handler(Looper.getMainLooper())
    private val reconcileBackstack: Runnable = Runnable {
        reconcileBackstack(pendingRemovals.toList(), mutableBackstack.value)
    }
    private val removeExitingFromBackstack: Runnable = Runnable {
        if (backstack.exiting == null) return@Runnable
        val nextBackstack = backstack.copy(
            exiting = null,
            exitingIndex = -1,
            isDirectUpdate = true
        )
        setBackstack(nextBackstack)
    }

    abstract val activeContext: NavigationContext<*>?
    abstract val isVisible: Boolean
    internal abstract val currentAnimations: NavigationAnimation

    private val pendingRemovals = mutableSetOf<AnyOpenInstruction>()
    private val mutableBackstack = MutableStateFlow(createEmptyBackStack())
    val backstackFlow: StateFlow<NavigationBackstack> get() = mutableBackstack
    val backstack: NavigationBackstack get() = backstackFlow.value

    @MainThread
    fun setBackstack(backstack: NavigationBackstack) = synchronized(this) {
        if (Looper.myLooper() != Looper.getMainLooper()) throw EnroException.NavigationContainerWrongThread(
            "A NavigationContainer's setBackstack method must only be called from the main thread"
        )
        if (backstack == backstackFlow.value) return@synchronized
        handler.removeCallbacks(reconcileBackstack)
        handler.removeCallbacks(removeExitingFromBackstack)

        requireBackstackIsAccepted(backstack)
        if(handleEmptyBehaviour(backstack)) return
        setActiveContainerFrom(backstack)

        val lastBackstack = mutableBackstack.getAndUpdate { backstack }

        val removed = lastBackstack.backstack
            .filter {
                !backstack.backstack.contains(it)
            }

        pendingRemovals.addAll(removed)
        val reconciledBackstack = reconcileBackstack(pendingRemovals.toList(), backstack)
        if (!reconciledBackstack) {
            handler.post(reconcileBackstack)
        } else {
            pendingRemovals.clear()
            handler.postDelayed(removeExitingFromBackstack, 2000)
        }
    }

    // Returns true if the backstack was able to be reconciled successfully
    protected abstract fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstack: NavigationBackstack
    ): Boolean

    fun accept(
        instruction: AnyOpenInstruction
    ): Boolean {
        return acceptsNavigationKey.invoke(instruction.navigationKey)
                && acceptsDirection(instruction.navigationDirection)
                && acceptsNavigator(parentContext.controller.navigatorForKeyType(instruction.navigationKey::class)
                ?: throw EnroException.UnreachableState()
        )
    }

    protected fun setOrLoadInitialBackstack(initialBackstack: NavigationBackstack) {
        val savedStateRegistry = parentContext.savedStateRegistryOwner.savedStateRegistry

        savedStateRegistry.unregisterSavedStateProvider(id)
        savedStateRegistry.registerSavedStateProvider(id) {
            bundleOf(
                BACKSTACK_KEY to ArrayList(backstack.backstack)
            )
        }

        parentContext.runWhenContextActive {
            if (!backstack.backstack.isEmpty()) return@runWhenContextActive
            val restoredBackstack = savedStateRegistry
                .consumeRestoredStateForKey(id)
                ?.getParcelableArrayList<AnyOpenInstruction>(BACKSTACK_KEY)
                ?.let { createRestoredBackStack(it) }
            setBackstack(restoredBackstack ?: initialBackstack)
        }
    }

    private fun requireBackstackIsAccepted(backstack: NavigationBackstack) {
        backstack.backstack
            .map {
                it.navigationDirection to acceptsDirection(it.navigationDirection)
            }
            .filter { !it.second }
            .map { it.first }
            .toSet()
            .let {
                require(it.isEmpty()) {
                    "Backstack does not support the following NavigationDirections: ${it.joinToString { it::class.java.simpleName }}"
                }
            }
    }

    private fun handleEmptyBehaviour(backstack: NavigationBackstack): Boolean {
        if (backstack.backstack.isEmpty()) {
            when (val emptyBehavior = emptyBehavior) {
                EmptyBehavior.AllowEmpty -> {
                    /* If allow empty, pass through to default behavior */
                }
                EmptyBehavior.CloseParent -> {
                    if (parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        parentContext.getNavigationHandle().close()
                    }
                    return true
                }
                is EmptyBehavior.Action -> {
                    return emptyBehavior.onEmpty()
                }
            }
        }
        return false
    }

    private fun setActiveContainerFrom(backstack: NavigationBackstack) {
        if (backstack.isDirectUpdate) return
        val isClosing = backstack.lastInstruction is NavigationInstruction.Close
        val isEmpty = backstack.backstack.isEmpty()

        if(!isClosing) {
            parentContext.containerManager.setActiveContainer(this)
            return
        }

        if (backstack.exiting != null) {
            parentContext.containerManager.setActiveContainerById(
                backstack.exiting.internal.previouslyActiveId
            )
        }

        if(isActive && isEmpty) parentContext.containerManager.setActiveContainer(null)
    }


    companion object {
        private const val BACKSTACK_KEY = "NavigationContainer.BACKSTACK_KEY"
    }
}

val NavigationContainer.isActive: Boolean
    get() = parentContext.containerManager.activeContainer == this

fun NavigationContainer.setActive() {
    parentContext.containerManager.setActiveContainer(this)
}
