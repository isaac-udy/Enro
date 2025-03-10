package dev.enro.core.compose.container

import android.os.Bundle
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.lifecycleScope
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationInstruction
import dev.enro.core.activity
import dev.enro.core.allParentContexts
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerBackEvent
import dev.enro.core.container.NavigationInstructionFilter
import dev.enro.core.container.getAnimationsForEntering
import dev.enro.core.container.getAnimationsForExiting
import dev.enro.core.container.merge
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.getNavigationHandle
import dev.enro.core.requestClose
import dev.enro.destination.compose.destination.AnimationEvent
import dev.enro.destination.flow.ManagedFlowNavigationBinding
import dev.enro.destination.flow.host.ComposableHostForManagedFlowDestination
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.Closeable
import kotlin.collections.set

public class ComposableNavigationContainer internal constructor(
    key: NavigationContainerKey,
    parentContext: NavigationContext<*>,
    instructionFilter: NavigationInstructionFilter,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
) : NavigationContainer(
    key = key,
    context = parentContext,
    contextType = ComposableDestination::class.java,
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    animations = animations,
    instructionFilter = instructionFilter,
) {
    private val viewModelStoreStorage: ComposableViewModelStoreStorage = parentContext.getComposableViewModelStoreStorage()
    private val viewModelStores = viewModelStoreStorage.getStorageForContainer(key)

    private val restoredDestinationState = mutableMapOf<String, Bundle>()
    private var destinationOwners by mutableStateOf<List<ComposableDestinationOwner>>(emptyList())

    override val isVisible: Boolean
        get() = true

    private val onDestroyLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event != Lifecycle.Event.ON_DESTROY) return@LifecycleEventObserver
        destroy()
    }.also { observer ->
        (listOf(context) + context.allParentContexts).forEach {
            it.lifecycleOwner.lifecycle.addObserver(observer)
        }
    }

    // When we've got a NavigationHost wrapping this ComposableNavigationContainer,
    // we want to take the animations provided by the NavigationHost's NavigationContainer,
    // and sometimes skip other animation jobs
    private val shouldTakeAnimationsFromParentContainer: Boolean
        get() = context.contextReference is NavigationHost
                && backstack.size <= 1
                && currentTransition.lastInstruction != NavigationInstruction.Close

    // We want "Render" to look like it's a Composable function (it's a Composable lambda), so
    // we are uppercasing the first letter of the property name, which triggers a PropertyName lint warning
    @Suppress("PropertyName")
    public val Render: @Composable () -> Unit = movableContentOf {
        key(key.name) {
            destinationOwners
                .forEach {
                    key(it.instruction.instructionId) {
                        it.Render(backstack)
                    }
                }
        }
    }

    init {
        backEvents
            .onEach { backEvent ->
                if (backEvent is NavigationContainerBackEvent.Confirmed) {
                    backEvent.context.getNavigationHandle().requestClose()
                }
            }
            .launchIn(context.lifecycleOwner.lifecycleScope)
    }

    public override fun save(): Bundle {
        val savedState = super.save()
        destinationOwners
            .filter { it.lifecycle.currentState != Lifecycle.State.DESTROYED }
            .forEach { destinationOwner ->
                savedState.putBundle(
                    DESTINATION_STATE_PREFIX_KEY + destinationOwner.instruction.instructionId,
                    destinationOwner.save()
                )
            }
        return savedState
    }

    public override fun restore(bundle: Bundle) {
        bundle.keySet()
            .forEach { key ->
                if (!key.startsWith(DESTINATION_STATE_PREFIX_KEY)) return@forEach
                val instructionId = key.removePrefix(DESTINATION_STATE_PREFIX_KEY)
                val restoredState = bundle.getBundle(key) ?: return@forEach
                restoredDestinationState[instructionId] = restoredState
            }
        super.restore(bundle)

        // After the backstack has been set, we're going to remove the restored states which aren't in the backstack
        val instructionsInBackstack = backstack.map { it.instructionId }.toSet()
        restoredDestinationState.keys.minus(instructionsInBackstack).forEach {
            restoredDestinationState.remove(it)
        }
    }

    override fun getChildContext(contextFilter: ContextFilter): NavigationContext<*>? {
        return when (contextFilter) {
            is ContextFilter.Active -> destinationOwners
                .lastOrNull { it.instruction == backstack.active }
                ?.destination
                ?.context

            is ContextFilter.ActivePushed -> destinationOwners
                .lastOrNull { it.instruction == backstack.activePushed }
                ?.destination
                ?.context

            is ContextFilter.ActivePresented -> destinationOwners
                .lastOrNull { it.instruction == backstack.activePresented }
                ?.destination
                ?.context

            is ContextFilter.WithId -> destinationOwners
                .lastOrNull { it.instruction.instructionId == contextFilter.id }
                ?.destination
                ?.context
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onBackstackUpdated(
        transition: NavigationBackstackTransition
    ): Boolean {
        if (!context.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
        if (context.runCatching { activity }.getOrNull() == null) return false

        val activeDestinations = destinationOwners
            .filter {
                it.lifecycle.currentState != Lifecycle.State.DESTROYED
            }
            .associateBy { it.instruction }
            .toMutableMap()

        backstack.forEach { instruction ->
            if (activeDestinations[instruction] == null) {
                activeDestinations[instruction] = createDestinationOwner(instruction)
            }
        }

        val visible = mutableSetOf<AnyOpenInstruction>()

        backstack.takeLastWhile { it.navigationDirection == NavigationDirection.Present }
            .forEach { visible.add(it) }

        backstack.lastOrNull { it.navigationDirection == NavigationDirection.Push }
            ?.let { visible.add(it) }

        destinationOwners.forEach {
            if (activeDestinations[it.instruction] == null) {
                it.animations.setAnimation(getAnimationsForExiting(it.instruction).asComposable())
                it.animations.setAnimationEvent(AnimationEvent.AnimateTo(false))
            }
        }
        destinationOwners = merge(transition.previousBackstack, transition.activeBackstack)
            .mapNotNull { instruction ->
                activeDestinations[instruction]
            }
        setVisibilityForBackstack(transition)
        return true
    }

    private fun createDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner {
        val controller = context.controller
        val composeKey = instruction.navigationKey
        val rawBinding = controller.bindingForKeyType(composeKey::class)
            ?: throw EnroException.MissingNavigationBinding(composeKey)

        if (rawBinding !is ComposableNavigationBinding<*, *> && rawBinding !is ManagedFlowNavigationBinding<*, *>) {
            throw IllegalStateException("Expected ${composeKey::class.java.simpleName} to be bound to a Composable, but was instead bound to a ${rawBinding.baseType.java.simpleName}")
        }
        // TODO:
        //  Instead of managing destination construction here, we should move this to the NavigationHostFactory,
        //  and let the NavigationHostFactory manage the destination construction. This means more significant changes
        //  to the way that the NavigationHostFactory works, so this is a future improvement.
        //  The cost of delaying this improvement is small at the moment, as the ComposableNavigationContainer is the only
        //  container that needs to manage destination construction in this way.
        val destination = when (rawBinding) {
            is ComposableNavigationBinding<*, *> -> {
                rawBinding.constructDestination()
            }
            is ManagedFlowNavigationBinding<*, *> -> {
                ComposableHostForManagedFlowDestination()
            }
            else -> error("")
        }

        val restoredState = restoredDestinationState.remove(instruction.instructionId)
        return ComposableDestinationOwner(
            parentContainer = this,
            instruction = instruction,
            destination = destination,
            viewModelStore = viewModelStores.getOrPut(instruction.instructionId) { ViewModelStore() },
            onNavigationContextCreated = context.controller.dependencyScope.get(),
            onNavigationContextSaved = context.controller.dependencyScope.get(),
            composeEnvironment = context.controller.dependencyScope.get(),
            savedInstanceState = restoredState,
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    private fun setVisibilityForBackstack(transition: NavigationBackstackTransition) {
        val isParentContextStarted = context.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (!isParentContextStarted && shouldTakeAnimationsFromParentContainer) return

        val isParentBeingRemoved = when {
            context.contextReference is Fragment && !context.contextReference.isAdded -> true
            else -> false
        }
        val presented =
            transition.activeBackstack.takeLastWhile { it.navigationDirection is NavigationDirection.Present }.toSet()
        val activePush = transition.activeBackstack.lastOrNull { it.navigationDirection !is NavigationDirection.Present }?.instructionId
        val activePresented = presented.lastOrNull()?.instructionId
        destinationOwners.forEach { destinationOwner ->
            val instruction = destinationOwner.instruction

            val isActive = when (instruction.instructionId) {
                activePresented -> !isParentBeingRemoved
                activePush -> !isParentBeingRemoved
                else -> false
            }
            val isClosing = transition.lastInstruction is NavigationInstruction.Close
            when {
                isActive && isClosing -> {
                    destinationOwner.animations.setAnimation(
                        getAnimationsForEntering(destinationOwner.instruction).asComposable()
                    )
                    destinationOwner.animations.setAnimationEvent(AnimationEvent.AnimateTo(true))
                }
                !isActive && isClosing -> {
                    destinationOwner.animations.setAnimation(
                        getAnimationsForExiting(destinationOwner.instruction).asComposable()
                    )
                    destinationOwner.animations.setAnimationEvent(AnimationEvent.AnimateTo(false))
                }
                isActive && !isClosing -> {
                    destinationOwner.animations.setAnimation(
                        getAnimationsForEntering(destinationOwner.instruction).asComposable()
                    )
                    destinationOwner.animations.setAnimationEvent(AnimationEvent.AnimateTo(true))
                }
                !isActive && !isClosing -> {
                    destinationOwner.animations.setAnimation(
                        getAnimationsForExiting(destinationOwner.instruction).asComposable()
                    )
                    destinationOwner.animations.setAnimationEvent(AnimationEvent.AnimateTo(false))
                }
            }
        }
    }

    /**
     * This is an Advanced Enro API, and should only be used in cases where you are certain that you want to
     * destroy the ComposableNavigationContainer.
     *
     * This is not recommended for general use, and is primarily provided for situations where a
     * NavigationContainer's lifecycle does not match the parent context's lifecycle.
     */
    @AdvancedEnroApi
    public fun manuallyDestroy() {
        destroy()
        viewModelStoreStorage.clearStorageForContainer(key)
    }

    private fun destroy() {
        destinationOwners.forEach { composableDestinationOwner ->
            composableDestinationOwner.destroy()
        }
        destinationOwners = emptyList()
        context.containerManager.removeContainer(this)
        context.savedStateRegistryOwner.savedStateRegistry.unregisterSavedStateProvider(key.name)
        (listOf(context) + context.allParentContexts).forEach {
            it.lifecycleOwner.lifecycle.removeObserver(onDestroyLifecycleObserver)
        }
        cancelJobs()
    }

    @Composable
    internal fun registerWithContainerManager(
        registrationStrategy: ContainerRegistrationStrategy,
        initialBackstack: NavigationBackstack,
    ): Boolean {
        val registration = remember(key, registrationStrategy) {
            val containerManager = context.containerManager
            containerManager.addContainer(this@ComposableNavigationContainer)
            Closeable { destroy() }
        }

        rememberSaveable<Unit> (
            init = {
                if (currentTransition === initialTransition) {
                    restoreOrSetBackstack(initialBackstack)
                }
                mutableStateOf(Unit)
            },
            stateSaver = object : Saver<Unit, Bundle> {
                override fun restore(value: Bundle) {
                    // When restoring, there are some cases where the active container is not the container that is being restored,
                    // and performing the restore might set that container to be active when that's not actually what we want,
                    // so we're going to remember the currently active container key, before performing the restore,
                    // and then re-set the active container afterwards.
                    val activeBeforeRestore = context.containerManager.activeContainer?.key
                    when (registrationStrategy) {
                        ContainerRegistrationStrategy.DisposeWithComposition -> this@ComposableNavigationContainer.restore(value)
                        ContainerRegistrationStrategy.DisposeWithCompositionDoNotSave -> Unit
                        ContainerRegistrationStrategy.DisposeWithLifecycle -> Unit
                    }
                    context.containerManager.setActiveContainerByKey(activeBeforeRestore)
                }

                override fun SaverScope.save(value: Unit): Bundle? = when(registrationStrategy) {
                    ContainerRegistrationStrategy.DisposeWithComposition -> this@ComposableNavigationContainer.save()
                    ContainerRegistrationStrategy.DisposeWithCompositionDoNotSave -> null
                    ContainerRegistrationStrategy.DisposeWithLifecycle -> null
                }
            }
        )

        DisposableEffect(key, registrationStrategy) {
            onDispose {
                when (registrationStrategy) {
                    ContainerRegistrationStrategy.DisposeWithComposition -> registration.close()
                    ContainerRegistrationStrategy.DisposeWithCompositionDoNotSave -> registration.close()
                    ContainerRegistrationStrategy.DisposeWithLifecycle -> {} // handled by init
                }
            }
        }

        DisposableEffect(key) {
            val containerManager = context.containerManager
            onDispose {
                if (!context.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return@onDispose
                if (containerManager.activeContainer == this@ComposableNavigationContainer) {
                    val previouslyActiveContainer = backstack.active?.internal?.previouslyActiveContainer?.takeIf { it != key }
                    containerManager.setActiveContainerByKey(previouslyActiveContainer)
                }
            }
        }

        DisposableEffect(key) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_PAUSE) {
                    setVisibilityForBackstack(NavigationBackstackTransition(backstack to backstack))
                    setBackstack(backstack)
                }
            }
            context.lifecycle.addObserver(lifecycleObserver)
            onDispose { context.lifecycle.removeObserver(lifecycleObserver) }
        }
        return true
    }

    private companion object {
        private const val DESTINATION_STATE_PREFIX_KEY = "DestinationState@"
    }
}

/**
 * The ContainerRegistrationStrategy defines how ComposableNavigationContainers are managed within the context
 * of a Composable function. This is used to determine when the container should destroy child destinations (and associated
 * resources such as ViewModels) and when the container should save and restore its state.
 *
 * By default, containers with dynamic NavigationContainerKeys use DisposeWithComposition, and containers with defined keys
 * are managed with DisposeWithLifecycle.
 *
 * DisposeWithLifecycle will keep a container active while the parent lifecycle is active. This means that ViewModels and other
 * resources will be kept alive, even if the container is not currently being rendered within the composition.
 *
 * DisposeWithComposition will keep a container active only while the container is in the composition, but will save the container's
 * state using the Composable rememberSaveable. This means that ViewModels and other resources will be destroyed when the
 * container is removed from the composition, but that when the container returns to the composition, it's state should be restored.
 *
 * DisposeWithCompositionDoNotSave will keep a container active only while the container is in the composition, and will not
 * save the container's state. This means that ViewModels and other resources will be destroyed when the container is removed from
 * the composition, and that when the container returns to the composition, it's state will not be restored. This behaviour
 * should be used only in advanced cases where multiple dynamic navigation containers are required, and there is some other
 * state saving management defined in application code using NavigationContainer's save/restore functions.
 *
 * This is an Advanced Enro API, and should only be used in cases where you are sure that you want to change the default behavior.
 */
@AdvancedEnroApi
public sealed interface ContainerRegistrationStrategy {
    public data object DisposeWithComposition : ContainerRegistrationStrategy
    public data object DisposeWithCompositionDoNotSave : ContainerRegistrationStrategy
    public data object DisposeWithLifecycle : ContainerRegistrationStrategy
}