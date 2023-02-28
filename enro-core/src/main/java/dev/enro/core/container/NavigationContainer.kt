package dev.enro.core.container

import android.os.Looper
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import dev.enro.core.*
import dev.enro.core.compatability.Compatibility
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.controller.usecase.CanInstructionBeHostedAs
import dev.enro.core.controller.usecase.GetNavigationAnimations
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

public abstract class NavigationContainer(
    public val key: NavigationContainerKey,
    public val contextType: Class<out Any>,
    public val parentContext: NavigationContext<*>,
    public val emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    public val acceptsNavigationKey: (NavigationKey) -> Boolean,
    public val acceptsDirection: (NavigationDirection) -> Boolean,
) {
    internal val getNavigationAnimations = parentContext.controller.dependencyScope.get<GetNavigationAnimations>()
    private val canInstructionBeHostedAs = parentContext.controller.dependencyScope.get<CanInstructionBeHostedAs>()

    internal val interceptor = NavigationInterceptorBuilder()
        .apply(interceptor)
        .build(parentContext.controller.dependencyScope)

    public abstract val activeContext: NavigationContext<*>?
    public abstract val isVisible: Boolean

    private val mutableBackstack: MutableStateFlow<NavigationBackstack> = MutableStateFlow(emptyBackstack())
    public val backstackFlow: StateFlow<NavigationBackstack> get() = mutableBackstack
    public val backstack: NavigationBackstack get() = backstackFlow.value

    public var lastTransition: NavigationBackstackTransition? = null
        private set

    public var currentTransition: NavigationBackstackTransition? = null
        private set

    private var renderJob: Job? = null

    init {
        parentContext.lifecycleOwner.lifecycleScope.launch {
            parentContext.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (currentTransition == null) return@repeatOnLifecycle
                performBackstackUpdate(NavigationBackstackTransition(backstack to backstack))
            }
        }
    }

    @MainThread
    public fun setBackstack(backstack: NavigationBackstack): Unit = synchronized(this) {
        if (Looper.myLooper() != Looper.getMainLooper()) throw EnroException.NavigationContainerWrongThread(
            "A NavigationContainer's setBackstack method must only be called from the main thread"
        )
        if (backstack == backstackFlow.value) return@synchronized
        renderJob?.cancel()
        val processedBackstack = Compatibility.NavigationContainer
            .processBackstackForDeprecatedInstructionTypes(backstack, acceptsNavigationKey)
            .ensureOpeningTypeIsSet(parentContext)
            .processBackstackForPreviouslyActiveContainer()

        requireBackstackIsAccepted(processedBackstack)
        if (handleEmptyBehaviour(processedBackstack)) return
        val lastBackstack = mutableBackstack.getAndUpdate { processedBackstack }
        val transition = NavigationBackstackTransition(lastBackstack to processedBackstack)
        setActiveContainerFrom(transition)
        performBackstackUpdate(transition)
    }

    private fun performBackstackUpdate(transition: NavigationBackstackTransition) {
        currentTransition = transition
        if (onBackstackUpdated(transition)) {
            lastTransition = transition
            return
        }
        renderJob = parentContext.lifecycleOwner.lifecycleScope.launch {
            parentContext.lifecycle.withCreated {}
            while (!onBackstackUpdated(transition) && isActive) {
                delay(16)
            }
            lastTransition = transition
        }
    }

    // Returns true if the backstack was able to be updated successfully
    protected abstract fun onBackstackUpdated(
        transition: NavigationBackstackTransition
    ) : Boolean

    public fun accept(
        instruction: AnyOpenInstruction
    ): Boolean {
        return (acceptsNavigationKey.invoke(instruction.navigationKey) || instruction.navigationDirection is NavigationDirection.Present)
                && acceptsDirection(instruction.navigationDirection)
                && canInstructionBeHostedAs(
            hostType = contextType,
            navigationContext = parentContext,
            instruction = instruction
        )
    }

    protected fun setOrLoadInitialBackstack(initialBackstack: NavigationBackstack) {
        val savedStateRegistry = parentContext.savedStateRegistryOwner.savedStateRegistry

        savedStateRegistry.unregisterSavedStateProvider(key.name)
        savedStateRegistry.registerSavedStateProvider(key.name) {
            bundleOf(
                BACKSTACK_KEY to ArrayList(backstack)
            )
        }

        val initialise = {
            val restoredBackstack = savedStateRegistry
                .consumeRestoredStateForKey(key.name)
                ?.getParcelableArrayList<AnyOpenInstruction>(BACKSTACK_KEY)
                ?.toBackstack()

            val backstack = (restoredBackstack ?: initialBackstack)
            setBackstack(backstack)
        }
        if (!savedStateRegistry.isRestored) {
            parentContext.lifecycleOwner.lifecycleScope.launch {
                parentContext.lifecycle.withCreated {
                    initialise()
                }
            }
        } else initialise()
    }

    private fun requireBackstackIsAccepted(backstack: List<AnyOpenInstruction>) {
        backstack
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

    private fun handleEmptyBehaviour(backstack: List<AnyOpenInstruction>): Boolean {
        if (backstack.isEmpty()) {
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

    private fun setActiveContainerFrom(backstackTransition: NavigationBackstackTransition) {
        // TODO
        if (!parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        if (!parentContext.containerManager.containers.contains(this)) return

        val isClosing = backstackTransition.lastInstruction is NavigationInstruction.Close
        val isEmpty = backstackTransition.activeBackstack.isEmpty()

        if (!isClosing) {
            parentContext.containerManager.setActiveContainer(this)
            return
        }

        if (backstackTransition.exitingInstruction != null) {
            parentContext.containerManager.setActiveContainerByKey(
                backstackTransition.exitingInstruction.internal.previouslyActiveContainer
            )
        }

        if (isActive && isEmpty) parentContext.containerManager.setActiveContainer(null)
    }

    private fun NavigationBackstack.processBackstackForPreviouslyActiveContainer(): NavigationBackstack {
        return map {
            if (it.internal.previouslyActiveContainer != null) return@map it
            it.internal.copy(
                previouslyActiveContainer = parentContext.containerManager.activeContainer?.key
            )
        }.toBackstack()
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

private fun NavigationContainer.getTransitionForInstruction(instruction: AnyOpenInstruction): NavigationBackstackTransition? {
    val isHosted = parentContext.contextReference is NavigationHost
    if (!isHosted) return currentTransition

    val parentContainer = parentContext.parentContainer() ?: return currentTransition
    val parentRoot = parentContainer.currentTransition?.activeBackstack?.getOrNull(0)
    val parentActive = parentContainer.currentTransition?.activeBackstack?.active
    val thisRoot = currentTransition?.activeBackstack?.getOrNull(0)
    if (parentRoot == thisRoot && parentRoot == parentActive) {
        val mergedPreviousBackstack = merge(
            currentTransition?.previousBackstack.orEmpty(),
            parentContainer.currentTransition?.previousBackstack.orEmpty()
        ).toBackstack()

        val mergedActiveBackstack = merge(
            currentTransition?.activeBackstack.orEmpty(),
            parentContainer.currentTransition?.activeBackstack.orEmpty()
        ).toBackstack()

        return NavigationBackstackTransition(mergedPreviousBackstack to mergedActiveBackstack)
    }

    val isRootInstruction = backstack.size <= 1
    if (!isRootInstruction) return currentTransition

    val isLastInstruction = parentContainer.currentTransition?.lastInstruction == instruction
    val isExitingInstruction = parentContainer.currentTransition?.exitingInstruction == instruction
    val isEnteringInstruction = parentContainer.currentTransition?.activeBackstack?.active == instruction

    if (isLastInstruction ||
        isExitingInstruction ||
        isEnteringInstruction
    ) return parentContainer.currentTransition

    return currentTransition
}

public fun NavigationContainer.getAnimationsForEntering(instruction: AnyOpenInstruction): NavigationAnimation {
    val animations = parentContext.controller.dependencyScope.get<GetNavigationAnimations>()
    val currentTransition = getTransitionForInstruction(instruction)
        ?: return animations.opening(null, instruction).entering

    val exitingInstruction = currentTransition.exitingInstruction
        ?: return animations.opening(null, instruction).entering

    if(currentTransition.lastInstruction is NavigationInstruction.Close) {
        return animations.closing(exitingInstruction, instruction).entering
    }
    return animations.opening(exitingInstruction, instruction).entering
}

public fun NavigationContainer.getAnimationsForExiting(instruction: AnyOpenInstruction): NavigationAnimation {
    val animations = parentContext.controller.dependencyScope.get<GetNavigationAnimations>()
    val currentTransition = getTransitionForInstruction(instruction)
        ?: return animations.closing(instruction, null).exiting

    val activeInstruction = currentTransition.activeBackstack.active
        ?: return animations.closing(instruction, null).exiting

    if(currentTransition.lastInstruction is NavigationInstruction.Close) {
        return animations.closing(instruction, activeInstruction).exiting
    }
    return animations.opening(instruction, activeInstruction).exiting
}