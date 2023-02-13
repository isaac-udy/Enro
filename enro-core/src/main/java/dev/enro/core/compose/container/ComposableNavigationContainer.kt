package dev.enro.core.compose.container

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelStore
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.*
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import kotlin.collections.set

public class ComposableNavigationContainer internal constructor(
    key: NavigationContainerKey,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    initialBackstack: NavigationBackstack
) : NavigationContainer(
    key = key,
    parentContext = parentContext,
    contextType = ComposableDestination::class.java,
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    acceptsNavigationKey = accept,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward || it is NavigationDirection.Present },
) {
    private val viewModelStoreStorage: ComposableViewModelStoreStorage = parentContext.getComposableViewModelStoreStorage()
    private val viewModelStores = viewModelStoreStorage.viewModelStores.getOrPut(key) { mutableMapOf() }

    private var saveableStateHolder: SaveableStateHolder? = null

    private var destinationOwners by mutableStateOf<List<ComposableDestinationOwner>>(emptyList())
    private val currentDestination
        get() = destinationOwners
            .lastOrNull {
                it.instruction == backstack.active
            }

    override val activeContext: NavigationContext<out ComposableDestination>?
        get() = currentDestination?.destination?.navigationContext

    override val isVisible: Boolean
        get() = true
    override var currentAnimations: NavigationAnimation = DefaultAnimations.none


    // When we've got a NavigationHost wrapping this ComposableNavigationContainer,
    // we want to take the animations provided by the NavigationHost's NavigationContainer,
    // and sometimes skip other animation jobs
    private val shouldTakeAnimationsFromParentContainer: Boolean
        get() = parentContext.contextReference is NavigationHost
                && backstack.size <= 1
                && lastInstruction != NavigationInstruction.Close

    // We want "Render" to look like it's a Composable function (it's a Composable lambda), so
    // we are uppercasing the first letter of the property name, which triggers a PropertyName lint warning
    @Suppress("PropertyName")
    public val Render: @Composable () -> Unit = movableContentOf {
        key(key.name) {
            saveableStateHolder?.SaveableStateProvider(key.name) {
                val backstack by backstackFlow.collectAsState()
                destinationOwners
                    .forEach {
                        it.Render(backstack)
                    }
            }
        }
    }

    init {
        setOrLoadInitialBackstack(initialBackstack)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onBackstackUpdated(
        transition: NavigationBackstackTransition
    ): Boolean {
        if (!parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
        if (parentContext.runCatching { activity }.getOrNull() == null) return false

        val activeDestinations = destinationOwners
            .filter {
                it.lifecycle.currentState != Lifecycle.State.DESTROYED
            }
            .associateBy { it.instruction }
            .toMutableMap()

        transition.previousBackstack
            .minus(transition.activeBackstack)
            .mapNotNull { activeDestinations[it]?.destination }
            .forEach {
                when(it) {
                    is DialogDestination -> it.dialogConfiguration.isDismissed.value = true
                    is BottomSheetDestination -> it.bottomSheetConfiguration.isDismissed.value = true
                }
            }

        backstack.forEach { instruction ->
            if(activeDestinations[instruction] == null) {
                activeDestinations[instruction] = createDestinationOwner(instruction)
            }
        }

        val visible = mutableSetOf<AnyOpenInstruction>()

        backstack.takeLastWhile { it.navigationDirection == NavigationDirection.Present }
            .forEach { visible.add(it) }

        backstack.lastOrNull { it.navigationDirection == NavigationDirection.Push }
            ?.let { visible.add(it) }

        destinationOwners.forEach {
            if(activeDestinations[it.instruction] == null) {
                it.transitionState.targetState = false
            }
        }
        destinationOwners = merge(transition.previousBackstack, transition.activeBackstack)
            .mapNotNull { instruction ->
                activeDestinations[instruction]
            }
        setAnimationsForBackstack(transition)
        setVisibilityForBackstack(transition)
        return true
    }

    private fun createDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner {
        val controller = parentContext.controller
        val composeKey = instruction.navigationKey
        val destination =
            (controller.bindingForKeyType(composeKey::class) as ComposableNavigationBinding<NavigationKey, ComposableDestination>)
                .constructDestination()

        return ComposableDestinationOwner(
            parentContainer = this,
            instruction = instruction,
            destination = destination,
            viewModelStore = viewModelStores.getOrPut(instruction.instructionId) { ViewModelStore() },
            onNavigationContextCreated = parentContext.controller.dependencyScope.get(),
            onNavigationContextSaved = parentContext.controller.dependencyScope.get(),
            composeEnvironment = parentContext.controller.dependencyScope.get(),
        )
    }

    private fun setAnimationsForBackstack(transition: NavigationBackstackTransition) {
        val contextForAnimation = when (lastInstruction) {
            is NavigationInstruction.Close -> destinationOwners.lastOrNull { it.instruction == transition.exitingInstruction }?.destination?.navigationContext
            else -> activeContext
        } ?: parentContext

        runCatching { parentContext.parentContext }.onFailure { return }
        val isRestoredFromExitingParent = when (parentContext.contextReference) {
            is NavigationHost -> parentContext.parentContainer()?.lastInstruction != null
            else -> false
        }

        val lastInstruction = lastInstruction
        currentAnimations = when {
            shouldTakeAnimationsFromParentContainer -> {
                val parentContainer = parentContext.parentContainer()
                parentContainer?.currentAnimations ?: DefaultAnimations.none
            }
            lastInstruction == null -> {
                if (!isRestoredFromExitingParent) DefaultAnimations.none
                val parentContainer = parentContext.parentContainer()
                parentContainer?.currentAnimations ?: DefaultAnimations.none
            }
            else -> animationsFor(contextForAnimation, lastInstruction)
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    private fun setVisibilityForBackstack(transition: NavigationBackstackTransition) {
        val isParentContextStarted = parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (!isParentContextStarted && shouldTakeAnimationsFromParentContainer) return

        val isParentBeingRemoved = when {
            parentContext.contextReference is Fragment && !parentContext.contextReference.isAdded -> true
            else -> false
        }
        val presented = transition.activeBackstack.takeLastWhile { it.navigationDirection is NavigationDirection.Present }.toSet()
        val activePush = transition.activeBackstack.lastOrNull { it.navigationDirection !is NavigationDirection.Present }
        destinationOwners.forEach { destinationOwner ->
            val instruction = destinationOwner.instruction
            destinationOwner.transitionState.targetState = when {
                destinationOwner.destination is BottomSheetDestination -> true
                destinationOwner.destination is DialogDestination -> true
                presented.contains(destinationOwner.instruction) -> !isParentBeingRemoved
                instruction == activePush -> !isParentBeingRemoved
                else -> false
            }
        }
    }

    @Composable
    internal fun registerWithContainerManager(
        registrationStrategy: ContainerRegistrationStrategy
    ): Boolean {
        saveableStateHolder = rememberSaveableStateHolder()
        DisposableEffect(Unit) {
            onDispose { saveableStateHolder = null }
        }

        DisposableEffect(key, registrationStrategy) {
            val containerManager = parentContext.containerManager

            fun dispose() {
                containerManager.removeContainer(this@ComposableNavigationContainer)
                destinationOwners.forEach { composableDestinationOwner ->
                    composableDestinationOwner.destroy()
                }
                destinationOwners = emptyList()
            }

            val lifecycleEventObserver = LifecycleEventObserver { _, event ->
                if (event != Lifecycle.Event.ON_DESTROY) return@LifecycleEventObserver
                dispose()
            }

            containerManager.addContainer(this@ComposableNavigationContainer)
            when (registrationStrategy) {
                ContainerRegistrationStrategy.DisposeWithComposition -> {}
                ContainerRegistrationStrategy.DisposeWithLifecycle -> parentContext.lifecycle.addObserver(lifecycleEventObserver)
            }
            onDispose {
                when (registrationStrategy) {
                    ContainerRegistrationStrategy.DisposeWithComposition -> dispose()
                    ContainerRegistrationStrategy.DisposeWithLifecycle -> parentContext.lifecycle.removeObserver(lifecycleEventObserver)
                }
            }
        }

        DisposableEffect(key) {
            val containerManager = parentContext.containerManager
            onDispose {
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
                    setAnimationsForBackstack(NavigationBackstackTransition(backstack to backstack))
                }
            }
            parentContext.lifecycle.addObserver(lifecycleObserver)
            onDispose { parentContext.lifecycle.removeObserver(lifecycleObserver) }
        }
        return true
    }
}

@AdvancedEnroApi
public sealed interface ContainerRegistrationStrategy {
    public object DisposeWithComposition : ContainerRegistrationStrategy
    public object DisposeWithLifecycle : ContainerRegistrationStrategy
}