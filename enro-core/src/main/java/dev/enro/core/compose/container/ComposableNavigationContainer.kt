package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager

class ComposableNavigationContainer internal constructor(
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
    acceptsNavigator = { it is ComposableNavigator<*, *> }
) {
    private val destinationStorage: ComposableDestinationOwnerStorage = parentContext.getComposableContextStorage()

    private val destinationContexts = destinationStorage.destinations.getOrPut(id) { mutableMapOf() }
    private val currentDestination get() = backstackFlow.value.backstack
        .mapNotNull { destinationContexts[it.instructionId] }
        .lastOrNull {
            it.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
        }

    override val activeContext: NavigationContext<*>?
        get() = currentDestination?.destination?.navigationContext

    override val isVisible: Boolean
        get() = true

    init {
        setOrLoadInitialBackstack(initialBackstack)
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstack: NavigationBackstack
    ): Boolean {
        backstack.renderable
            .map { instruction ->
                val context = requireDestinationContext(instruction)
                if(backstack.isDirectUpdate) {
                    context.animation = DefaultAnimations.none.asComposable()
                }
            }

        val contextForAnimation = kotlin.runCatching {
            when (backstack.lastInstruction) {
                is NavigationInstruction.Close -> backstack.exiting?.let { getDestinationContext(it) }?.destination?.navigationContext
                else -> backstack.active?.let { getDestinationContext(it) }?.destination?.navigationContext
            }
        }.getOrNull()
        if(contextForAnimation != null) {
            val animations = animationsFor(contextForAnimation, backstack.lastInstruction).asComposable()
            backstack.exiting?.let {
                requireDestinationContext(it).animation = animations
            }
            backstack.active?.let {
                requireDestinationContext(it).animation = animations
            }
        }

        removed
            .filter { backstack.exiting != it }
            .mapNotNull {
                destinationContexts[it.instructionId]
            }
            .forEach {
                destinationContexts.remove(it.instruction.instructionId)
            }

        return true
    }

    internal fun getDestinationContext(instruction: AnyOpenInstruction): ComposableDestinationOwner? {
        return destinationContexts[instruction.instructionId]
    }

    internal fun requireDestinationContext(instruction: AnyOpenInstruction): ComposableDestinationOwner {
        return destinationContexts.getOrPut(instruction.instructionId) {
            val controller = parentContext.controller
            val composeKey = instruction.navigationKey
            val destination = controller.navigatorForKeyType(composeKey::class)!!.contextType.java
                .newInstance() as ComposableDestination

            return@getOrPut ComposableDestinationOwner(
                parentContainer = this,
                instruction = instruction,
                destination = destination,
            ).also { owner ->
                owner.lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if(event != Lifecycle.Event.ON_DESTROY) return
                        destinationContexts.remove(owner.instruction.instructionId)
                    }
                })
            }
        }.apply { parentContainer = this@ComposableNavigationContainer }
    }
}

@Composable
internal fun NavigationContainerManager.registerState(controller: ComposableNavigationContainer): Boolean {
    DisposableEffect(controller.id) {
        addContainer(controller)
        if (activeContainer == null) {
            setActiveContainer(controller)
        }
        onDispose {
            removeContainer(controller)
            if (activeContainer == controller) {
                setActiveContainer(null)
            }
        }
    }
    return true
}