package dev.enro.core.compose.container

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelStore
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.merge
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import kotlin.collections.set

public class ComposableNavigationContainer internal constructor(
    key: NavigationContainerKey,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    initialBackstack: List<AnyOpenInstruction>
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
                it.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
            }

    override val activeContext: NavigationContext<out ComposableDestination>?
        get() = currentDestination?.destination?.navigationContext

    override val isVisible: Boolean
        get() = true
    override val currentAnimations: NavigationAnimation
        get() = NavigationAnimation.Resource(0, 0)

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

    override fun renderBackstack(
        previousBackstack: List<AnyOpenInstruction>,
        backstack: List<AnyOpenInstruction>
    ): Boolean {
        if (!parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
        if (parentContext.runCatching { activity }.getOrNull() == null) return false

        val activeDestinations = destinationOwners
            .filter {
                it.lifecycle.currentState != Lifecycle.State.DESTROYED
            }
            .associateBy { it.instruction }
            .toMutableMap()

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

        destinationOwners = merge(previousBackstack, backstack)
            .onEach {
                activeDestinations[it]?.transitionState?.targetState = visible.contains(it)
            }
            .mapNotNull { instruction ->
                activeDestinations[instruction]
            }
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
                    val previouslyActiveContainer = backstack.lastOrNull()?.internal?.previouslyActiveContainer?.takeIf { it != key }
                    containerManager.setActiveContainerByKey(previouslyActiveContainer)
                }
            }
        }
        return true
    }
}

@AdvancedEnroApi
public sealed interface ContainerRegistrationStrategy {
    public object DisposeWithComposition : ContainerRegistrationStrategy
    public object DisposeWithLifecycle : ContainerRegistrationStrategy
}