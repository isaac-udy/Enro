package dev.enro.core.compose.container

import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
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

    private var saveableStateRegistry: SaveableStateRegistry? by mutableStateOf(null)

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

    // When we've got a NavigationHost wrapping this ComposableNavigationContainer,
    // we want to take the animations provided by the NavigationHost's NavigationContainer,
    // and sometimes skip other animation jobs
    private val shouldTakeAnimationsFromParentContainer: Boolean
        get() = parentContext.contextReference is NavigationHost
                && backstack.size <= 1
                && currentTransition?.lastInstruction != NavigationInstruction.Close

    public fun save() : Map<String, List<Any?>>? {
        return saveableStateRegistry?.performSave()?.also {
            Log.e("COMPOSABLE", "saving $it")
        }
    }

    public fun restore(state: Map<String, List<Any?>>?) {
        Log.e("COMPOSABLE", "restoring $state")
        val backstack = state?.get("example") as? NavigationBackstack
        saveableStateRegistry = SaveableStateRegistry(
            canBeSaved = { true },
            restoredValues = state
        )

        setBackstack(backstack ?: return)
    }

    // We want "Render" to look like it's a Composable function (it's a Composable lambda), so
    // we are uppercasing the first letter of the property name, which triggers a PropertyName lint warning
    @Suppress("PropertyName")
    public val Render: @Composable () -> Unit = movableContentOf {
        key(key.name) {
            val backstack by backstackFlow.collectAsState()
            if (saveableStateRegistry != null) {
                CompositionLocalProvider(
                    LocalSaveableStateRegistry provides saveableStateRegistry
                ) {
                    destinationOwners
                        .forEach {
                            it.Render(backstack)
                        }
                }
            }
        }
    }

    init {
        setOrLoadInitialBackstack(initialBackstack)
        val lifecycleEventObserver = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_DESTROY) return@LifecycleEventObserver
            destroy()
        }
        parentContext.lifecycle.addObserver(lifecycleEventObserver)
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
                viewModelStores.remove(it.instruction.instructionId)?.clear()
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
        val activePresented = presented.lastOrNull()
        destinationOwners.forEach { destinationOwner ->
            val instruction = destinationOwner.instruction
            val isPushedDialogOrBottomSheet = ((destinationOwner.destination is DialogDestination || destinationOwner.destination is BottomSheetDestination) && activePresented != null)

            destinationOwner.transitionState.targetState = when (instruction) {
                activePresented -> !isParentBeingRemoved
                activePush -> !isParentBeingRemoved && !isPushedDialogOrBottomSheet
                else -> false
            }
        }
    }

    private fun destroy() {
        destinationOwners.forEach { composableDestinationOwner ->
            composableDestinationOwner.destroy()
        }
        destinationOwners = emptyList()
    }

    @Composable
    internal fun registerWithContainerManager(
        registrationStrategy: ContainerRegistrationStrategy
    ): Boolean {
        val localSaveableStateRegistry = LocalSaveableStateRegistry.current
        DisposableEffect(localSaveableStateRegistry) {
            if(localSaveableStateRegistry == null) return@DisposableEffect onDispose {  }

            val restored = localSaveableStateRegistry.consumeRestored(key.name)
            restore(restored as Map<String, List<Any?>>?)
            val entry = localSaveableStateRegistry.registerProvider(key.name) {
                saveableStateRegistry?.performSave().orEmpty().plus("example" to backstack)
            }

            onDispose { entry.unregister() }
        }
//        saveableStateHolder = rememberSaveableStateHolder()
//        DisposableEffect(Unit) {
//            onDispose { saveableStateHolder = null }
//        }

        DisposableEffect(key, registrationStrategy) {
            val containerManager = parentContext.containerManager
            containerManager.addContainer(this@ComposableNavigationContainer)
            onDispose {
                when (registrationStrategy) {
                    ContainerRegistrationStrategy.DisposeWithComposition -> destroy()
                    ContainerRegistrationStrategy.DisposeWithLifecycle -> {} // handled by init
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