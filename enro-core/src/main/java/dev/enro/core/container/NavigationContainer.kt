package dev.enro.core.container

import android.os.Bundle
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import dev.enro.animation.DefaultAnimations
import dev.enro.animation.NavigationAnimation
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.compatability.Compatibility
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationInstruction
import dev.enro.core.close
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.controller.usecase.CanInstructionBeHostedAs
import dev.enro.core.controller.usecase.GetNavigationAnimations
import dev.enro.core.findContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.leafContext
import dev.enro.core.parentContainer
import dev.enro.core.requestClose
import dev.enro.core.result.EnroResult
import dev.enro.core.rootContext
import dev.enro.extensions.getParcelableListCompat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

public abstract class NavigationContainer(
    public val key: NavigationContainerKey,
    public val contextType: Class<out Any>,
    public val context: NavigationContext<*>,
    public val emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
    public val instructionFilter: NavigationInstructionFilter,
) : NavigationContainerContext {
    internal val dependencyScope by lazy {
        NavigationContainerScope(
            owner = this,
            animations = animations
        )
    }
    internal val getNavigationAnimations = dependencyScope.get<GetNavigationAnimations>()
    private val canInstructionBeHostedAs = dependencyScope.get<CanInstructionBeHostedAs>()

    internal val interceptor = NavigationInterceptorBuilder()
        .apply(interceptor)
        .build()

    public val childContext: NavigationContext<*>? get() = getChildContext(ContextFilter.Active)
    public abstract val isVisible: Boolean

    public override val isActive: Boolean
        get() = context.containerManager.activeContainer == this

    public override fun setActive() {
        context.containerManager.setActiveContainer(this)
        val parent = parentContainer() ?: return
        if (parent != this) parent.setActive()
    }

    private val mutableBackstackFlow: MutableStateFlow<NavigationBackstack> =
        MutableStateFlow(initialBackstack)
    public override val backstackFlow: StateFlow<NavigationBackstack> get() = mutableBackstackFlow

    private var mutableBackstack by mutableStateOf(initialBackstack)
    public override val backstack: NavigationBackstack by derivedStateOf { mutableBackstack }

    public var currentTransition: NavigationBackstackTransition = initialTransition

    private var renderJob: Job? = null
    private val backstackUpdateJob = context.lifecycleOwner.lifecycleScope.launch {
        context.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (currentTransition === initialTransition) return@repeatOnLifecycle
            performBackstackUpdate(NavigationBackstackTransition(initialBackstack to backstack))
        }
    }

    @CallSuper
    public override fun save(): Bundle {
        return bundleOf(
            BACKSTACK_KEY to ArrayList(backstack)
        )
    }

    @CallSuper
    public override fun restore(bundle: Bundle) {
        val restoredBackstack = bundle.getParcelableListCompat<AnyOpenInstruction>(BACKSTACK_KEY)
            .orEmpty()
            .toBackstack()

        setBackstack(restoredBackstack)
    }

    /**
     * This exists to expose a way for the ComposableNavigationContainer to cancel
     * long running lifecycle related coroutines, which is specifically useful for the manualDestroy
     * functionality that exists for ComposableNavigationContainer, as these containers can have
     * slightly different lifecycles to those of the navigation context they are contained within
     */
    protected fun cancelJobs() {
        renderJob?.cancel()
        backstackUpdateJob.cancel()
    }

    @MainThread
    public override fun setBackstack(backstack: NavigationBackstack): Unit = synchronized(this) {
        if (Looper.myLooper() != Looper.getMainLooper()) throw EnroException.NavigationContainerWrongThread(
            "A NavigationContainer's setBackstack method must only be called from the main thread"
        )
        renderJob?.cancel()
        val processedBackstack = Compatibility.NavigationContainer
            .processBackstackForDeprecatedInstructionTypes(backstack, instructionFilter)
            .filterBackstackForForwardedResults()
            .ensureOpeningTypeIsSet(context)
            .processBackstackForPreviouslyActiveContainer()

        if (processedBackstack == backstackFlow.value) return@synchronized
        if (handleEmptyBehaviour(processedBackstack)) return
        val lastBackstack = mutableBackstack
        mutableBackstack = processedBackstack
        mutableBackstackFlow.value = mutableBackstack
        val transition = NavigationBackstackTransition(lastBackstack to processedBackstack)
        setActiveContainerFrom(transition)
        performBackstackUpdate(transition)
    }

    private fun performBackstackUpdate(transition: NavigationBackstackTransition) {
        currentTransition = transition
        if (onBackstackUpdated(transition)) {
            return
        }
        renderJob = context.lifecycleOwner.lifecycleScope.launch {
            context.lifecycle.withCreated {}
            while (!onBackstackUpdated(transition) && isActive) {
                delay(16)
            }
        }
    }

    public abstract fun getChildContext(contextFilter: ContextFilter): NavigationContext<*>?

    // Returns true if the backstack was able to be updated successfully
    protected abstract fun onBackstackUpdated(
        transition: NavigationBackstackTransition
    ): Boolean

    private fun acceptedByContext(navigationInstruction: NavigationInstruction.Open<*>): Boolean {
        if (context.contextReference !is NavigationHost) return true
        return context.contextReference.accept(navigationInstruction)
    }

    public fun accept(
        instruction: AnyOpenInstruction
    ): Boolean {
        val isPresentedWithLegacyBehavior = context.controller.config.useLegacyContainerPresentBehavior
                && instruction.navigationDirection == NavigationDirection.Present

        return (instructionFilter.accept(instruction) || isPresentedWithLegacyBehavior)
                && acceptedByContext(instruction)
                && canInstructionBeHostedAs(
            hostType = contextType,
            navigationContext = context,
            instruction = instruction
        )
    }

    protected fun restoreOrSetBackstack(backstack: NavigationBackstack) {
        val savedStateRegistry = context.savedStateRegistryOwner.savedStateRegistry

        savedStateRegistry.unregisterSavedStateProvider(key.name)
        savedStateRegistry.registerSavedStateProvider(key.name) { save() }

        val initialise = {
            val savedState = savedStateRegistry.consumeRestoredStateForKey(key.name)
            when (savedState) {
                null -> setBackstack(backstack)
                else -> restore(savedState)
            }
        }
        if (!savedStateRegistry.isRestored) {
            context.lifecycleOwner.lifecycleScope.launch {
                context.lifecycle.withCreated {
                    initialise()
                }
            }
        } else initialise()
    }

    private fun handleEmptyBehaviour(backstack: List<AnyOpenInstruction>): Boolean {
        if (backstack.isEmpty()) {
            when (val emptyBehavior = emptyBehavior) {
                EmptyBehavior.AllowEmpty -> {
                    /* If allow empty, pass through to default behavior */
                }

                EmptyBehavior.CloseParent -> {
                    if (context.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        context.getNavigationHandle().requestClose()
                    }
                    return true
                }

                EmptyBehavior.ForceCloseParent -> {
                    if (context.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        context.getNavigationHandle().close()
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
        if (!context.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        if (!context.containerManager.containers.contains(this)) return

        val isClosing = backstackTransition.lastInstruction is NavigationInstruction.Close
        val isEmpty = backstackTransition.activeBackstack.isEmpty()

        if (!isClosing) {
            setActive()
            return
        }

        if (backstackTransition.exitingInstruction != null) {
            val previouslyActiveContainer = backstackTransition.exitingInstruction.internal.previouslyActiveContainer
            if (previouslyActiveContainer != null) {
                context.rootContext()
                    .findContainer(previouslyActiveContainer)
                    ?.setActive()
            }
        }

        if (isActive && isEmpty) context.containerManager.setActiveContainer(null)
    }

    private fun NavigationBackstack.processBackstackForPreviouslyActiveContainer(): NavigationBackstack {
        return map {
            if (it.internal.previouslyActiveContainer != null) return@map it
            it.internal.copy(
                previouslyActiveContainer = context.rootContext().leafContext().parentContainer()?.key
            )
        }.toBackstack()
    }

    // When using result forwarding, a NavigationContainer can be restored (or otherwise have the backstack set) for
    // instructions that have a pending result already applied in the EnroResult result manager. In these cases,
    // we want to filter out the instructions that already have a result applied. This is to ensure that when result
    // forwarding happens across multiple containers, all destinations providing the result are closed, even if those
    // destinations aren't visible/active when the forwarded result is added to EnroResult.
    private fun NavigationBackstack.filterBackstackForForwardedResults(): NavigationBackstack {
        val enroResult = EnroResult.from(context.controller)
        return filter { !enroResult.hasPendingResultFrom(it) }.toBackstack()
    }

    public sealed class ContextFilter {
        public data object Active : ContextFilter()
        public data object ActivePresented : ContextFilter()
        public data object ActivePushed : ContextFilter()
        public data class WithId(val id: String) : ContextFilter()
    }

    public companion object {
        private const val BACKSTACK_KEY = "NavigationContainer.BACKSTACK_KEY"
        internal val initialBackstack = emptyBackstack()
        internal val initialTransition =
            NavigationBackstackTransition(initialBackstack to initialBackstack)
    }
}

private fun NavigationContainer.getTransitionForInstruction(instruction: AnyOpenInstruction): NavigationBackstackTransition {
    val isHosted = context.contextReference is NavigationHost
    if (!isHosted) return currentTransition

    val parentContainer = context.parentContainer() ?: return currentTransition
    val parentRoot = parentContainer.currentTransition.activeBackstack.getOrNull(0)
    val parentActive = parentContainer.currentTransition.activeBackstack.active
    val thisRoot = currentTransition.activeBackstack.getOrNull(0)
    if (parentRoot == thisRoot && parentRoot == parentActive) {
        val mergedPreviousBackstack = merge(
            currentTransition.previousBackstack,
            parentContainer.currentTransition.previousBackstack
        ).toBackstack()

        val mergedActiveBackstack = merge(
            currentTransition.activeBackstack.orEmpty(),
            parentContainer.currentTransition.activeBackstack.orEmpty()
        ).toBackstack()

        return NavigationBackstackTransition(mergedPreviousBackstack to mergedActiveBackstack)
    }

    val isRootInstruction =
        backstack.size <= 1 || backstack.firstOrNull()?.instructionId == instruction.instructionId
    if (!isRootInstruction) return currentTransition

    val isLastInstruction = parentContainer.currentTransition.lastInstruction == instruction
    val isExitingInstruction =
        parentContainer.currentTransition.exitingInstruction?.instructionId == instruction.instructionId
    val isEnteringInstruction =
        parentContainer.currentTransition.activeBackstack.active?.instructionId == instruction.instructionId

    if (isLastInstruction ||
        isExitingInstruction ||
        isEnteringInstruction
    ) return parentContainer.currentTransition

    return currentTransition
}

public fun NavigationContainer.getAnimationsForEntering(instruction: AnyOpenInstruction): NavigationAnimation {
    val animations = dependencyScope.get<GetNavigationAnimations>()
    val currentTransition = getTransitionForInstruction(instruction)

    val isInitialInstruction =
        currentTransition.previousBackstack.identity == NavigationContainer.initialBackstack.identity
    if (isInitialInstruction) {
        return DefaultAnimations.noOp.entering
    }

    val exitingInstruction = currentTransition.exitingInstruction
        ?: return animations.opening(null, instruction).entering

    if (currentTransition.lastInstruction is NavigationInstruction.Close) {
        return animations.closing(exitingInstruction, instruction).entering
    }
    return animations.opening(exitingInstruction, instruction).entering
}

public fun NavigationContainer.getAnimationsForExiting(instruction: AnyOpenInstruction): NavigationAnimation {
    val animations = dependencyScope.get<GetNavigationAnimations>()
    val currentTransition = getTransitionForInstruction(instruction)

    val activeInstruction = currentTransition.activeBackstack.active
        ?: return animations.closing(instruction, null).exiting

    val closingNonActiveInstruction = !currentTransition.activeBackstack.contains(instruction)
            && currentTransition.previousBackstack.contains(instruction)
            && currentTransition.previousBackstack.indexOf(instruction) < currentTransition.previousBackstack.lastIndex

    if (
        currentTransition.lastInstruction is NavigationInstruction.Close ||
        backstack.isEmpty() ||
        (!currentTransition.activeBackstack.contains(instruction) && !closingNonActiveInstruction)
    ) {
        return animations.closing(instruction, activeInstruction).exiting
    }
    return animations.opening(instruction, activeInstruction).exiting
}