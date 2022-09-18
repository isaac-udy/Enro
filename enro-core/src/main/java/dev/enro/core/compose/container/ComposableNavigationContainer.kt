package dev.enro.core.compose.container

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableDestinationContextReference
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.compose.getComposableDestinationContext
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationBackstack
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
    private val destinationStorage: ComposableContextStorage = parentContext.getComposableContextStorage()

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

    val animation: MutableState<NavigationAnimation.Composable> = mutableStateOf( DefaultAnimations.none.asComposable() )

    init {
        setOrLoadInitialBackstack(initialBackstack)
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstack: NavigationBackstack
    ): Boolean {
        backstack.renderable
            .map { instruction ->
                requireDestinationContext(instruction)
            }

        if(!backstack.isDirectUpdate) {
            activeContext?.let {
                animation.value =
                    animationsFor(it, backstack.lastInstruction).asComposable()
            }
        } else {
            animation.value = DefaultAnimations.none.asComposable()
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

    internal fun getDestinationContext(instruction: AnyOpenInstruction): ComposableDestinationContextReference? {
        return destinationContexts[instruction.instructionId]
    }

    internal fun requireDestinationContext(instruction: AnyOpenInstruction): ComposableDestinationContextReference {
        return destinationContexts.getOrPut(instruction.instructionId) {
            val controller = parentContext.controller
            val composeKey = instruction.navigationKey
            val destination = controller.navigatorForKeyType(composeKey::class)!!.contextType.java
                .newInstance() as ComposableDestination

            return@getOrPut getComposableDestinationContext(
                instruction = instruction,
                destination = destination,
                parentContainer = this
            )
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