package dev.enro.core.compose.container

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelStore
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.components.ContainerAcceptPolicy
import dev.enro.core.container.components.ContainerActivePolicy
import dev.enro.core.container.components.ContainerAnimationPolicy
import dev.enro.core.container.components.ContainerContextProvider
import dev.enro.core.container.components.ContainerEmptyPolicy
import dev.enro.core.container.components.ContainerRenderer
import dev.enro.core.container.components.ContainerState
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import java.io.Closeable

public class ComposableNavigationContainer internal constructor(
    key: NavigationContainerKey,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
    initialBackstack: NavigationBackstack,
    containerContextProvider: ContainerContextProvider<ComposableDestination> = object :
        ContainerContextProvider<ComposableDestination> {
        override fun getActiveNavigationContext(backstack: NavigationBackstack): NavigationContext<ComposableDestination>? {
            return null
        }

        override fun getContext(instruction: AnyOpenInstruction): ComposableDestination? {
            return null
        }

        override fun createContext(instruction: AnyOpenInstruction): ComposableDestination {
            TODO("Not yet implemented")
        }

        override fun bind(state: ContainerState) {

        }

        override fun destroy() {

        }
    },
) : NavigationContainer(
    key = key,
    initialBackstack = initialBackstack,
    context = parentContext,
    interceptor = interceptor,
    animations = animations,
    acceptPolicy = ContainerAcceptPolicy.Default(
        context = parentContext,
        acceptsContextType = Fragment::class,
        acceptsNavigationKey = accept,
    ),
    activePolicy = ContainerActivePolicy.Default(
        key = key,
        context = parentContext
    ),
    emptyPolicy = ContainerEmptyPolicy.Default(
        context = parentContext,
        emptyBehavior = emptyBehavior,
    ),
    animationPolicy = ContainerAnimationPolicy.Default(),
    containerRenderer = object : ContainerRenderer {
        override val isVisible: Boolean
            get() = true

        override fun bind(state: ContainerState) {

        }

        override fun destroy() {

        }
    },
    containerContextProvider = containerContextProvider
) {
    private val viewModelStoreStorage: ComposableViewModelStoreStorage =
        parentContext.getComposableViewModelStoreStorage()
    private val viewModelStores = viewModelStoreStorage.getStorageForContainer(key)

    private val restoredDestinationState = mutableMapOf<String, Bundle>()
    private var destinationOwners by mutableStateOf<List<ComposableDestinationOwner>>(emptyList())
    private val currentDestination by derivedStateOf {
        destinationOwners
            .lastOrNull {
                it.instruction == backstack.active
            }
    }

    public val isAnimating: Boolean by derivedStateOf {
        destinationOwners.any { !it.transitionState.isIdle }
    }

    private val onDestroyLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event != Lifecycle.Event.ON_DESTROY) return@LifecycleEventObserver
        destroy()
    }.also { observer ->
        parentContext.lifecycle.addObserver(observer)
    }

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
//
//    public override fun save(): Bundle {
//        val savedState = super.save()
//        destinationOwners
//            .filter { it.lifecycle.currentState != Lifecycle.State.DESTROYED }
//            .forEach { destinationOwner ->
//                savedState.putBundle(
//                    DESTINATION_STATE_PREFIX_KEY + destinationOwner.instruction.instructionId,
//                    destinationOwner.save()
//                )
//            }
//        return savedState
//    }
//
//    public override fun restore(bundle: Bundle) {
//        bundle.keySet()
//            .forEach { key ->
//                if (!key.startsWith(DESTINATION_STATE_PREFIX_KEY)) return@forEach
//                val instructionId = key.removePrefix(DESTINATION_STATE_PREFIX_KEY)
//                val restoredState = bundle.getBundle(key) ?: return@forEach
//                restoredDestinationState[instructionId] = restoredState
//            }
//        super.restore(bundle)
//    }


//    @OptIn(ExperimentalMaterialApi::class)
//    override fun onBackstackUpdated(
//        transition: NavigationBackstackTransition
//    ): Boolean {
//        if (!context.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
//        if (context.runCatching { activity }.getOrNull() == null) return false
//
//        val activeDestinations = destinationOwners
//            .filter {
//                it.lifecycle.currentState != Lifecycle.State.DESTROYED
//            }
//            .associateBy { it.instruction }
//            .toMutableMap()
//
//        transition.removed
//            .mapNotNull { activeDestinations[it]?.destination }
//            .forEach {
//                when (it) {
//                    is DialogDestination -> it.dialogConfiguration.isDismissed.value = true
//                    is BottomSheetDestination -> it.bottomSheetConfiguration.isDismissed.value =
//                        true
//                }
//            }
//
//        backstack.forEach { instruction ->
//            if (activeDestinations[instruction] == null) {
//                activeDestinations[instruction] = createDestinationOwner(instruction)
//            }
//        }
//
//        val visible = mutableSetOf<AnyOpenInstruction>()
//
//        backstack.takeLastWhile { it.navigationDirection == NavigationDirection.Present }
//            .forEach { visible.add(it) }
//
//        backstack.lastOrNull { it.navigationDirection == NavigationDirection.Push }
//            ?.let { visible.add(it) }
//
//        destinationOwners.forEach {
//            if (activeDestinations[it.instruction] == null) {
//                it.transitionState.targetState = false
//            }
//        }
//        destinationOwners = merge(transition.previousBackstack, transition.activeBackstack)
//            .mapNotNull { instruction ->
//                activeDestinations[instruction]
//            }
//        setVisibilityForBackstack(transition)
//        return true
//    }

    private fun createDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner {
        val controller = context.controller
        val composeKey = instruction.navigationKey
        val rawBinding = controller.bindingForKeyType(composeKey::class)
            ?: throw EnroException.MissingNavigationBinding(composeKey)

        if (rawBinding !is ComposableNavigationBinding<*, *>) {
            throw IllegalStateException("Expected ${composeKey::class.java.simpleName} to be bound to a Composable, but was instead bound to a ${rawBinding.baseType.java.simpleName}")
        }
        val destination = rawBinding.constructDestination()

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

//    @OptIn(ExperimentalMaterialApi::class)
//    private fun setVisibilityForBackstack(transition: NavigationBackstackTransition) {
//        val isParentContextStarted =
//            context.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
//        if (!isParentContextStarted && shouldTakeAnimationsFromParentContainer) return
//
//        val isParentBeingRemoved = when {
//            context.contextReference is Fragment && !context.contextReference.isAdded -> true
//            else -> false
//        }
//        val presented =
//            transition.activeBackstack.takeLastWhile { it.navigationDirection is NavigationDirection.Present }
//                .toSet()
//        val activePush =
//            transition.activeBackstack.lastOrNull { it.navigationDirection !is NavigationDirection.Present }
//        val activePresented = presented.lastOrNull()
//        destinationOwners.forEach { destinationOwner ->
//            val instruction = destinationOwner.instruction
//            val isPushedDialogOrBottomSheet =
//                ((destinationOwner.destination is DialogDestination || destinationOwner.destination is BottomSheetDestination) && activePresented != null)
//
//            destinationOwner.transitionState.targetState = when (instruction) {
//                activePresented -> !isParentBeingRemoved
//                activePush -> !isParentBeingRemoved && !isPushedDialogOrBottomSheet
//                else -> false
//            }
//        }
//    }

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
        context.lifecycleOwner.lifecycle.removeObserver(onDestroyLifecycleObserver)
    }

    @Composable
    internal fun registerWithContainerManager(
        registrationStrategy: ContainerRegistrationStrategy
    ): Boolean {
        val registration = remember(key, registrationStrategy) {
            val containerManager = context.containerManager
            containerManager.addContainer(this@ComposableNavigationContainer)
            Closeable { destroy() }
        }
        DisposableEffect(key, registrationStrategy) {
            onDispose {
                when (registrationStrategy) {
                    ContainerRegistrationStrategy.DisposeWithComposition -> registration.close()
                    ContainerRegistrationStrategy.DisposeWithLifecycle -> {} // handled by init
                }
            }
        }

        DisposableEffect(key) {
            val containerManager = context.containerManager
            onDispose {
                if (containerManager.activeContainer == this@ComposableNavigationContainer) {
                    val previouslyActiveContainer =
                        backstack.active?.internal?.previouslyActiveContainer?.takeIf { it != key }
                    containerManager.setActiveContainerByKey(previouslyActiveContainer)
                }
            }
        }

        DisposableEffect(key) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_PAUSE) {
//                    setVisibilityForBackstack(NavigationBackstackTransition(backstack to backstack))
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

@AdvancedEnroApi
public sealed interface ContainerRegistrationStrategy {
    public object DisposeWithComposition : ContainerRegistrationStrategy
    public object DisposeWithLifecycle : ContainerRegistrationStrategy
}