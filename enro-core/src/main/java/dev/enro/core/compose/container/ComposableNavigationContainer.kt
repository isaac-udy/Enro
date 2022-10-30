package dev.enro.core.compose.container

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstackState
import dev.enro.core.container.NavigationContainer
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.hosts.AbstractFragmentHostForComposable
import dev.enro.core.internal.get
import java.util.concurrent.ConcurrentHashMap

public class ComposableNavigationContainer internal constructor(
    id: String,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    internal val saveableStateHolder: SaveableStateHolder,
    initialBackstackState: NavigationBackstackState
) : NavigationContainer(
    id = id,
    parentContext = parentContext,
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    acceptsNavigationKey = accept,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward },
    acceptsBinding = { it is ComposableNavigationBinding<*, *> }
) {
    private val destinationStorage: ComposableViewModelStoreStorage = parentContext.getComposableViewModelStoreStorage()

    private val viewModelStores = destinationStorage.viewModelStores.getOrPut(id) { mutableMapOf() }
    private val destinationOwners = ConcurrentHashMap<String, ComposableDestinationOwner>()
    private val currentDestination
        get() = backstackFlow.value.backstack
            .mapNotNull { destinationOwners[it.instructionId] }
            .lastOrNull {
                it.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
            }

    // When we've got a FragmentHostForComposable wrapping this ComposableNavigationContainer,
    // we want to take the animations provided by the FragmentHostForComposable's NavigationContainer,
    // and sometimes skip other animation jobs
    private val shouldTakeAnimationsFromParentContainer: Boolean
        get() = parentContext.contextReference is AbstractFragmentHostForComposable
                    && backstack.backstack.size <= 1
                    && backstack.lastInstruction != NavigationInstruction.Close

    override val activeContext: NavigationContext<out ComposableDestination>?
        get() = currentDestination?.destination?.navigationContext

    override val isVisible: Boolean
        get() = true

    override var currentAnimations: NavigationAnimation = DefaultAnimations.none
        private set

    // We want "Render" to look like it's a Composable function (it's a Composable lambda), so
    // we are uppercasing the first letter of the property name, which triggers a PropertyName lint warning
    @Suppress("PropertyName")
    public val Render: @Composable () -> Unit = movableContentOf {
        key(id) {
            saveableStateHolder.SaveableStateProvider(id) {
                val backstackState by backstackFlow.collectAsState()

                backstackState.renderable
                    .mapNotNull { getDestinationOwner(it) }
                    .forEach {
                        it.Render(backstackState)
                    }
            }
        }
    }

    init {
        setOrLoadInitialBackstack(initialBackstackState)
        parentContext.lifecycleOwner.lifecycleScope.launchWhenStarted {
            setVisibilityForBackstack(backstack)
        }
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstackState: NavigationBackstackState
    ): Boolean {
        if (!parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
        if (parentContext.runCatching { activity }.getOrNull() == null) return false

        clearDestinationOwnersFor(removed)
        createDestinationOwnersFor(backstackState)
        setAnimationsForBackstack(backstackState)
        setVisibilityForBackstack(backstackState)
        return true
    }

    internal fun getDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner? =
        destinationOwners[instruction.instructionId]

    internal fun requireDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner {
        return destinationOwners.getOrPut(instruction.instructionId) {
            val controller = parentContext.controller
            val composeKey = instruction.navigationKey
            val destination =
                (controller.bindingForKeyType(composeKey::class) as ComposableNavigationBinding<NavigationKey, ComposableDestination>)
                    .constructDestination()

            return@getOrPut ComposableDestinationOwner(
                parentContainer = this,
                instruction = instruction,
                destination = destination,
                viewModelStore = viewModelStores.getOrPut(instruction.instructionId) { ViewModelStore()  },
                contextLifecycleController = parentContext.controller.dependencyScope.get(),
            ).also { owner ->
                owner.lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event != Lifecycle.Event.ON_DESTROY) return
                        destinationOwners.remove(owner.instruction.instructionId)
                    }
                })
            }
        }
    }

    private fun clearDestinationOwnersFor(removed: List<AnyOpenInstruction>) =
        removed
            .filter { backstack.exiting != it }
            .mapNotNull {
                destinationOwners[it.instructionId]
            }
            .forEach {
                it.destroy()
                destinationOwners.remove(it.instruction.instructionId)
            }

    private fun createDestinationOwnersFor(backstackState: NavigationBackstackState) {
        backstackState.renderable
            .forEach { instruction ->
                requireDestinationOwner(instruction)
            }
    }

    private fun setAnimationsForBackstack(backstackState: NavigationBackstackState) {
        val contextForAnimation = when (backstackState.lastInstruction) {
            is NavigationInstruction.Close -> backstackState.exiting?.let { getDestinationOwner(it) }?.destination?.navigationContext
            else -> activeContext
        } ?: parentContext

        currentAnimations = when {
            backstackState.isRestoredState -> DefaultAnimations.none
            shouldTakeAnimationsFromParentContainer -> {
                parentContext as FragmentContext<out Fragment>
                val parentContainer = parentContext.parentContainer()
                parentContainer?.currentAnimations ?: DefaultAnimations.none
            }
            backstackState.isInitialState -> DefaultAnimations.none
            else -> animationsFor(contextForAnimation, backstackState.lastInstruction)
        }.asComposable()
    }

    private fun setVisibilityForBackstack(backstackState: NavigationBackstackState) {
        val isParentContextStarted = parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if(!isParentContextStarted && shouldTakeAnimationsFromParentContainer) return

        val isParentBeingRemoved = when {
            parentContext.contextReference is Fragment && !parentContext.contextReference.isAdded -> true
            else -> false
        }
        backstackState.renderable.forEach {
            val destinationOwner = requireDestinationOwner(it)
            destinationOwner.transitionState.targetState = when (it) {
                backstackState.active -> !isParentBeingRemoved
                else -> false
            }
        }
    }

    @Composable
    internal fun registerWithContainerManager(): Boolean {
        DisposableEffect(id) {
            onDispose {
                destinationOwners.values.forEach { composableDestinationOwner ->
                    composableDestinationOwner.destroy()
                }
                destinationOwners.clear()
            }
        }

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
