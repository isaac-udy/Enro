package dev.enro.core.compose.container

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstackState
import dev.enro.core.container.NavigationContainer
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import java.util.concurrent.ConcurrentHashMap

public class ComposableNavigationContainer internal constructor(
    key: NavigationContainerKey,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    initialBackstackState: NavigationBackstackState
) : NavigationContainer(
    key = key,
    parentContext = parentContext,
    contextType = ComposableDestination::class.java,
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    acceptsNavigationKey = accept,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward || it is NavigationDirection.Present },
) {
    private val destinationStorage: ComposableViewModelStoreStorage = parentContext.getComposableViewModelStoreStorage()
    private var saveableStateHolder: SaveableStateHolder? = null

    private val viewModelStores = destinationStorage.viewModelStores.getOrPut(key) { mutableMapOf() }
    private val destinationOwners = ConcurrentHashMap<String, ComposableDestinationOwner>()
    private val currentDestination
        get() = backstackFlow.value.backstack
            .mapNotNull { destinationOwners[it.instructionId] }
            .lastOrNull {
                it.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
            }

    // When we've got a NavigationHost wrapping this ComposableNavigationContainer,
    // we want to take the animations provided by the NavigationHost's NavigationContainer,
    // and sometimes skip other animation jobs
    private val shouldTakeAnimationsFromParentContainer: Boolean
        get() = parentContext.contextReference is NavigationHost
                    && backstackState.backstack.size <= 1
                    && backstackState.lastInstruction != NavigationInstruction.Close

    override val activeContext: NavigationContext<out ComposableDestination>?
        get() = currentDestination?.destination?.navigationContext

    override val isVisible: Boolean
        get() = true

    override var currentAnimations: NavigationAnimation by mutableStateOf(DefaultAnimations.none)
        private set

    // We want "Render" to look like it's a Composable function (it's a Composable lambda), so
    // we are uppercasing the first letter of the property name, which triggers a PropertyName lint warning
    @Suppress("PropertyName")
    public val Render: @Composable () -> Unit = movableContentOf {
        key(key.name) {
            saveableStateHolder?.SaveableStateProvider(key.name) {
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
            setVisibilityForBackstack(backstackState)
        }
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstackState: NavigationBackstackState
    ): Boolean {
        if (!parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
        if (parentContext.runCatching { activity }.getOrNull() == null) return false

        dismissDialogs(removed)
        clearDestinationOwnersFor(removed)
        createDestinationOwnersFor(backstackState)
        setVisibilityForBackstack(backstackState)
        setAnimationsForBackstack(backstackState)
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
                viewModelStore = viewModelStores.getOrPut(instruction.instructionId) { ViewModelStore() },
                onNavigationContextCreated = parentContext.controller.dependencyScope.get(),
                onNavigationContextSaved = parentContext.controller.dependencyScope.get(),
                composeEnvironment = parentContext.controller.dependencyScope.get(),
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

    @OptIn(ExperimentalMaterialApi::class)
    private fun dismissDialogs(removed: List<AnyOpenInstruction>) {
        removed
            .mapNotNull {
                destinationOwners[it.instructionId]
            }
            .map { it.destination }
            .forEach {
                when(it) {
                    is DialogDestination -> it.dialogConfiguration.isDismissed.value = true
                    is BottomSheetDestination -> it.bottomSheetConfiguration.isDismissed.value = true
                }
            }
    }

    private fun clearDestinationOwnersFor(removed: List<AnyOpenInstruction>) =
        removed
            .filter { backstackState.exiting != it }
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

        val isRestoredFromExitingParent = when {
            parentContext is FragmentContext<*> && parentContext.contextReference.isDetached -> return
            parentContext.contextReference is NavigationHost -> parentContext.parentContainer()?.backstackState?.exiting != null
            else -> false
        }

        currentAnimations = when {
            backstackState.isRestoredState -> {
                if (!isRestoredFromExitingParent) DefaultAnimations.none
                val parentContainer = parentContext.parentContainer()
                parentContainer?.currentAnimations ?: DefaultAnimations.none
            }
            shouldTakeAnimationsFromParentContainer -> {
                val parentContainer = parentContext.parentContainer()
                parentContainer?.currentAnimations ?: DefaultAnimations.none
            }
            backstackState.isInitialState -> DefaultAnimations.none
            else -> animationsFor(contextForAnimation, backstackState.lastInstruction)
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    private fun setVisibilityForBackstack(backstackState: NavigationBackstackState) {
        val isParentContextStarted = parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (!isParentContextStarted && shouldTakeAnimationsFromParentContainer) return

        val isParentBeingRemoved = when {
            parentContext.contextReference is Fragment && !parentContext.contextReference.isAdded -> true
            else -> false
        }
        val presented = backstackState.renderable.takeLastWhile { it.navigationDirection is NavigationDirection.Present }.toSet()
        val activePush = backstackState.backstack.lastOrNull { it.navigationDirection !is NavigationDirection.Present }
        backstackState.renderable.forEach {
            val destinationOwner = requireDestinationOwner(it)
            destinationOwner.transitionState.targetState = when {
                presented.contains(it) -> !isParentBeingRemoved && !(
                        it == backstackState.exiting
                                && destinationOwner.destination !is BottomSheetDestination
                                && destinationOwner.destination !is DialogDestination
                        )
                it == activePush -> !isParentBeingRemoved
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
                destinationOwners.values.forEach { composableDestinationOwner ->
                    composableDestinationOwner.destroy()
                }
                destinationOwners.clear()
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
                    val previouslyActiveContainer = backstackState.active?.internal?.previouslyActiveContainer?.takeIf { it != key }
                    containerManager.setActiveContainerByKey(previouslyActiveContainer)
                }
            }
        }

        DisposableEffect(key) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_PAUSE) {
                    setVisibilityForBackstack(backstackState)
                    setAnimationsForBackstack(backstackState)
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