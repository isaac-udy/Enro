package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.hosts.AbstractFragmentHostForComposable

public class ComposableNavigationContainer internal constructor(
    id: String,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    internal val saveableStateHolder: SaveableStateHolder,
    initialBackstack: NavigationBackstack
) : NavigationContainer(
    id = id,
    parentContext = parentContext,
    emptyBehavior = emptyBehavior,
    acceptsNavigationKey = accept,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward },
    acceptsBinding = { it is ComposableNavigationBinding<*, *> }
) {
    private val destinationStorage: ComposableDestinationOwnerStorage = parentContext.getComposableContextStorage()

    private val destinationOwners = destinationStorage.destinations.getOrPut(id) { mutableMapOf() }
    private val currentDestination
        get() = backstackFlow.value.backstack
            .mapNotNull { destinationOwners[it.instructionId] }
            .lastOrNull {
                it.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
            }

    override val activeContext: NavigationContext<out ComposableDestination>?
        get() = currentDestination?.destination?.navigationContext

    override val isVisible: Boolean
        get() = true

    override var currentAnimations: NavigationAnimation = DefaultAnimations.none
        private set

    init {
        setOrLoadInitialBackstack(initialBackstack)
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstack: NavigationBackstack
    ): Boolean {
        if (!parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
        if (parentContext.runCatching { activity }.getOrNull() == null) return false

        clearDestinationOwnersFor(removed)
        createDestinationOwnersFor(backstack)
        setAnimationsForBackstack(backstack)
        setVisibilityForBackstack(backstack)
        return true
    }

    internal fun getDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner? =
        synchronized(destinationOwners) {
            return destinationOwners[instruction.instructionId]
        }

    internal fun requireDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner =
        synchronized(destinationOwners) {
            return destinationOwners.getOrPut(instruction.instructionId) {
                val controller = parentContext.controller
                val composeKey = instruction.navigationKey
                val destination =
                    controller.bindingForKeyType(composeKey::class)!!.destinationType.java
                        .newInstance() as ComposableDestination

                return@getOrPut ComposableDestinationOwner(
                    parentContainer = this,
                    instruction = instruction,
                    destination = destination,
            ).also { owner ->
                owner.lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event != Lifecycle.Event.ON_DESTROY) return
                        synchronized(destinationOwners) {
                            destinationOwners.remove(owner.instruction.instructionId)
                        }
                    }
                })
            }
        }.apply { parentContainer = this@ComposableNavigationContainer }
    }

    private fun clearDestinationOwnersFor(removed: List<AnyOpenInstruction>) =
        synchronized(destinationOwners) {
            removed
                .filter { backstack.exiting != it }
                .mapNotNull {
                    destinationOwners[it.instructionId]
                }
                .forEach {
                    destinationOwners.remove(it.instruction.instructionId)
                }
        }

    private fun createDestinationOwnersFor(backstack: NavigationBackstack) {
        backstack.renderable
            .forEach { instruction ->
                requireDestinationOwner(instruction)
            }
    }

    private fun setAnimationsForBackstack(backstack: NavigationBackstack) {
        val shouldTakeAnimationsFromParentContainer = parentContext is FragmentContext<out Fragment>
                && parentContext.contextReference is AbstractFragmentHostForComposable
                && backstack.backstack.size <= 1
                && backstack.lastInstruction != NavigationInstruction.Close

        val contextForAnimation = when (backstack.lastInstruction) {
            is NavigationInstruction.Close -> backstack.exiting?.let { getDestinationOwner(it) }?.destination?.navigationContext
            else -> activeContext
        } ?: parentContext

        currentAnimations = when {
            backstack.isRestoredState -> DefaultAnimations.none
            shouldTakeAnimationsFromParentContainer -> {
                parentContext as FragmentContext<out Fragment>
                val parentContainer = parentContext.parentContainer()
                parentContainer?.currentAnimations ?: DefaultAnimations.none
            }
            else -> animationsFor(contextForAnimation, backstack.lastInstruction)
        }.asComposable()
    }

    private fun setVisibilityForBackstack(backstack: NavigationBackstack) {
        val isParentBeingRemoved = when {
            parentContext.contextReference is Fragment && !parentContext.contextReference.isAdded -> true
            else -> false
        }
        backstack.renderable.forEach {
            requireDestinationOwner(it).transitionState.targetState = when (it) {
                backstack.active -> !isParentBeingRemoved
                else -> false
            }
        }
    }

    @Composable
    internal fun registerWithContainerManager(): Boolean {
        DisposableEffect(id) {
            val containerManager = parentContext.containerManager
            containerManager.addContainer(this@ComposableNavigationContainer)
            if (containerManager.activeContainer == null) {
                containerManager.setActiveContainer(this@ComposableNavigationContainer)
            }
            onDispose {
                containerManager.removeContainer(this@ComposableNavigationContainer)
                if (containerManager.activeContainer == this@ComposableNavigationContainer) {
                    val previouslyActive = backstack.active?.internal?.previouslyActiveId?.takeIf { it != id }
                    containerManager.setActiveContainerById(previouslyActive)
                }
            }
        }

        DisposableEffect(id) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event != Lifecycle.Event.ON_PAUSE) return@LifecycleEventObserver
                if (parentContext.contextReference is Fragment && !parentContext.contextReference.isAdded) {
                    setAnimationsForBackstack(backstack)
                    setVisibilityForBackstack(backstack)
                }
            }
            parentContext.lifecycle.addObserver(lifecycleObserver)
            onDispose {
                parentContext.lifecycle.removeObserver(lifecycleObserver)
            }
        }
        return true
    }
}
