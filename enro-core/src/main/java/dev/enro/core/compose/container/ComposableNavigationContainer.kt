package dev.enro.core.compose.container

import android.os.Bundle
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
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
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
    initialBackstack: NavigationBackstack,
) : NavigationContainer(
    key = key,
    parentContext = parentContext,
    contextType = ComposableDestination::class.java,
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    animations = animations,
    acceptsNavigationKey = accept,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward || it is NavigationDirection.Present },
) {
    private val viewModelStoreStorage: ComposableViewModelStoreStorage = parentContext.getComposableViewModelStoreStorage()
    private val viewModelStores = viewModelStoreStorage.viewModelStores.getOrPut(key) { mutableMapOf() }

    private val restoredDestinationState = mutableMapOf<String, Bundle>()
    private var destinationOwners by mutableStateOf<List<ComposableDestinationOwner>>(emptyList())
    private val currentDestination by derivedStateOf {
        destinationOwners
            .lastOrNull {
                it.instruction == backstack.active
            }
    }

    override val activeContext: NavigationContext<out ComposableDestination>? by derivedStateOf {
        currentDestination?.destination?.context
    }

    override val isVisible: Boolean
        get() = true

    public val isAnimating: Boolean by derivedStateOf {
        destinationOwners.any { !it.transitionState.isIdle }
    }

    // When we've got a NavigationHost wrapping this ComposableNavigationContainer,
    // we want to take the animations provided by the NavigationHost's NavigationContainer,
    // and sometimes skip other animation jobs
    private val shouldTakeAnimationsFromParentContainer: Boolean
        get() = parentContext.contextReference is NavigationHost
                && backstack.size <= 1
                && currentTransition?.lastInstruction != NavigationInstruction.Close

    // We want "Render" to look like it's a Composable function (it's a Composable lambda), so
    // we are uppercasing the first letter of the property name, which triggers a PropertyName lint warning
    @Suppress("PropertyName")
    public val Render: @Composable () -> Unit = movableContentOf {
        key(key.name) {
            destinationOwners
                .forEach {
                    it.Render(backstack)
                }
        }
    }

    init {
        val lifecycleEventObserver = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_DESTROY) return@LifecycleEventObserver
            destroy()
        }
        parentContext.lifecycle.addObserver(lifecycleEventObserver)
        restoreOrSetBackstack(initialBackstack)
    }

    public override fun save(): Bundle {
        val savedState = super.save()
        destinationOwners
            .filter { it.lifecycle.currentState != Lifecycle.State.DESTROYED }
            .forEach { destinationOwner ->
                savedState.putBundle(DESTINATION_STATE_PREFIX_KEY + destinationOwner.instruction.instructionId, destinationOwner.save())
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

        transition.removed
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
        setVisibilityForBackstack(transition)
        return true
    }

    private fun createDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner {
        val controller = parentContext.controller
        val composeKey = instruction.navigationKey
        val destination =
            (controller.bindingForKeyType(composeKey::class) as ComposableNavigationBinding<NavigationKey, ComposableDestination>)
                .constructDestination()

        val restoredState = restoredDestinationState.remove(instruction.instructionId)
        return ComposableDestinationOwner(
            parentContainer = this,
            instruction = instruction,
            destination = destination,
            viewModelStore = viewModelStores.getOrPut(instruction.instructionId) { ViewModelStore() },
            onNavigationContextCreated = parentContext.controller.dependencyScope.get(),
            onNavigationContextSaved = parentContext.controller.dependencyScope.get(),
            composeEnvironment = parentContext.controller.dependencyScope.get(),
            savedInstanceState = restoredState,
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

    private companion object {
        private const val DESTINATION_STATE_PREFIX_KEY = "DestinationState@"
    }
}

@AdvancedEnroApi
public sealed interface ContainerRegistrationStrategy {
    public object DisposeWithComposition : ContainerRegistrationStrategy
    public object DisposeWithLifecycle : ContainerRegistrationStrategy
}