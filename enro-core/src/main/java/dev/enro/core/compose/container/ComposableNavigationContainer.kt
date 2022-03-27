package dev.enro.core.compose.container

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.lifecycle.Lifecycle
import dev.enro.core.*
import dev.enro.core.compose.*
import dev.enro.core.compose.ComposableDestinationContextReference
import dev.enro.core.compose.getComposableDestinationContext
import dev.enro.core.container.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    override fun reconcileBackstack(
        removed: List<NavigationContainerBackstackEntry>,
        backstack: NavigationContainerBackstack
    ): Boolean {
        removed
            .mapNotNull {
                destinationContexts[it.instruction.instructionId]
            }
            .forEach {
                destinationContexts.remove(it.instruction.instructionId)
                it.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        return true
    }

    internal fun onInstructionDisposed(instruction: NavigationInstruction.Open) {
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

    internal fun getDestinationContext(instruction: NavigationInstruction.Open): ComposableDestinationContextReference {
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