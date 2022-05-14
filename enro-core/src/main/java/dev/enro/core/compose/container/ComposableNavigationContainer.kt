package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.lifecycle.Lifecycle
import dev.enro.core.*
import dev.enro.core.compose.*
import dev.enro.core.compose.ComposableDestinationContextReference
import dev.enro.core.compose.getComposableDestinationContext
import dev.enro.core.container.*

class ComposableNavigationContainer internal constructor(
    id: String,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    internal val saveableStateHolder: SaveableStateHolder
) : NavigationContainer(
    id = id,
    parentContext = parentContext,
    accept = accept,
    emptyBehavior = emptyBehavior,
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

    override fun reconcileBackstack(
        removed: List<OpenPushInstruction>,
        backstack: NavigationContainerBackstack
    ): Boolean {
        removed
            .mapNotNull {
                destinationContexts[it.instructionId]
            }
            .forEach {
                destinationContexts.remove(it.instruction.instructionId)
                it.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        return true
    }

    internal fun onInstructionDisposed(instruction: AnyOpenInstruction) {
        val backstack = backstackFlow.value
        if (backstack.exiting == instruction) {
            setBackstack(
                backstack.copy(
                    exiting = null,
                    exitingIndex = -1,
                    isDirectUpdate = true
                )
            )
        }
    }

    internal fun getDestinationContext(instruction: AnyOpenInstruction): ComposableDestinationContextReference {
        val destinationContextReference = destinationContexts.getOrPut(instruction.instructionId) {
            val controller = parentContext.controller
            val composeKey = instruction.navigationKey
            val destination = controller.navigatorForKeyType(composeKey::class)!!.contextType.java
                .newInstance() as ComposableDestination

            return@getOrPut getComposableDestinationContext(
                instruction = instruction,
                destination = destination,
                parentContainer = this
            )
        }
        destinationContextReference.parentContainer = this@ComposableNavigationContainer
        return destinationContextReference
    }
}

@Composable
internal fun NavigationContainerManager.registerState(controller: ComposableNavigationContainer): Boolean {
    DisposableEffect(controller.id) {
        addContainer(controller)
        if (activeContainer == null) {
            activeContainerState.value = controller
        }
        onDispose {
            removeContainer(controller)
            if (activeContainer == controller) {
                activeContainerState.value = null
            }
        }
    }
    return true
}