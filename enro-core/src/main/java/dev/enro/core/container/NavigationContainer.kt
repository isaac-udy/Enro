package dev.enro.core.container

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import dev.enro.core.*
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.controller.usecase.CanInstructionBeHostedAs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate

public abstract class NavigationContainer(
    public val key: NavigationContainerKey,
    public val contextType: Class<out Any>,
    public val parentContext: NavigationContext<*>,
    public val emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    public val acceptsNavigationKey: (NavigationKey) -> Boolean,
    public val acceptsDirection: (NavigationDirection) -> Boolean,
) {
    private val canInstructionBeHostedAs = parentContext.controller.dependencyScope.get<CanInstructionBeHostedAs>()
    private val handler = Handler(Looper.getMainLooper())
    private val reconcileBackstack: Runnable = Runnable {
        reconcileBackstack(pendingRemovals.toList(), mutableBackstack.value)
    }
    private val removeExitingFromBackstack: Runnable = Runnable {
        if (backstackState.exiting == null) return@Runnable
        val nextBackstack = backstackState.copy(
            exiting = null,
            exitingIndex = -1,
            updateType = NavigationBackstackState.UpdateType.RESTORED_STATE
        )
        setBackstack(nextBackstack)
    }

    internal val interceptor = NavigationInterceptorBuilder()
        .apply(interceptor)
        .build(parentContext.controller.dependencyScope)

    public abstract val activeContext: NavigationContext<*>?
    public abstract val isVisible: Boolean
    internal abstract val currentAnimations: NavigationAnimation

    private val pendingRemovals = mutableSetOf<AnyOpenInstruction>()
    private val mutableBackstack = MutableStateFlow(createEmptyBackStack())
    public val backstackFlow: StateFlow<NavigationBackstackState> get() = mutableBackstack
    public val backstackState: NavigationBackstackState get() = backstackFlow.value

    init {
        parentContext.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_DESTROY) return@LifecycleEventObserver
            handler.removeCallbacks(reconcileBackstack)
            handler.removeCallbacks(removeExitingFromBackstack)
        })
    }

    @MainThread
    public fun setBackstack(backstackState: NavigationBackstackState): Unit = synchronized(this) {
        if (Looper.myLooper() != Looper.getMainLooper()) throw EnroException.NavigationContainerWrongThread(
            "A NavigationContainer's setBackstack method must only be called from the main thread"
        )
        if (backstackState == backstackFlow.value) return@synchronized
        handler.removeCallbacks(reconcileBackstack)
        handler.removeCallbacks(removeExitingFromBackstack)
        val processedBackstack = backstackState.ensureOpeningTypeIsSet(parentContext)

        requireBackstackIsAccepted(processedBackstack)
        if (handleEmptyBehaviour(processedBackstack)) return
        setActiveContainerFrom(processedBackstack)

        val lastBackstack = mutableBackstack.getAndUpdate { processedBackstack }

        val removed = lastBackstack.backstack
            .filter {
                !processedBackstack.backstack.contains(it)
            }

        pendingRemovals.addAll(removed)
        val reconciledBackstack = reconcileBackstack(pendingRemovals.toList(), processedBackstack)
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
        backstackState: NavigationBackstackState
    ): Boolean

    public fun accept(
        instruction: AnyOpenInstruction
    ): Boolean {
        return acceptsNavigationKey.invoke(instruction.navigationKey)
                && acceptsDirection(instruction.navigationDirection)
                && canInstructionBeHostedAs(
            hostType = contextType,
            navigationContext = parentContext,
            instruction = instruction
        )
    }

    protected fun setOrLoadInitialBackstack(initialBackstackState: NavigationBackstackState) {
        val savedStateRegistry = parentContext.savedStateRegistryOwner.savedStateRegistry

        savedStateRegistry.unregisterSavedStateProvider(key.name)
        savedStateRegistry.registerSavedStateProvider(key.name) {
            bundleOf(
                BACKSTACK_KEY to ArrayList(backstackState.backstack)
            )
        }

        val initialise = {
            val restoredBackstack = savedStateRegistry
                .consumeRestoredStateForKey(key.name)
                ?.getParcelableArrayList<AnyOpenInstruction>(BACKSTACK_KEY)
                ?.let { createRestoredBackStack(it) }

            val backstack = (restoredBackstack ?: initialBackstackState)
            setBackstack(backstack)
        }
        if (!savedStateRegistry.isRestored) {
            parentContext.lifecycleOwner.lifecycleScope.launchWhenCreated {
                initialise()
            }
        } else initialise()
    }

    private fun requireBackstackIsAccepted(backstackState: NavigationBackstackState) {
        backstackState.backstack
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

    private fun handleEmptyBehaviour(backstackState: NavigationBackstackState): Boolean {
        if (backstackState.backstack.isEmpty()) {
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

    private fun setActiveContainerFrom(backstackState: NavigationBackstackState) {
        if (backstackState.isRestoredState || backstackState.isInitialState) return
        val isClosing = backstackState.lastInstruction is NavigationInstruction.Close
        val isEmpty = backstackState.backstack.isEmpty()

        if (!isClosing) {
            parentContext.containerManager.setActiveContainer(this)
            return
        }

        if (backstackState.exiting != null) {
            parentContext.containerManager.setActiveContainerByKey(
                backstackState.exiting.internal.previouslyActiveContainer
            )
        }

        if (isActive && isEmpty) parentContext.containerManager.setActiveContainer(null)
    }


    public companion object {
        private const val BACKSTACK_KEY = "NavigationContainer.BACKSTACK_KEY"

        public val presentationContainer: NavigationContainerKey = NavigationContainerKey.FromName("NavigationContainer.presentationContainer")
    }
}

public val NavigationContainer.isActive: Boolean
    get() = parentContext.containerManager.activeContainer == this

public fun NavigationContainer.setActive() {
    parentContext.containerManager.setActiveContainer(this)
}
