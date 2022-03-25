package dev.enro.core.compose.container

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.lifecycle.Lifecycle
import dev.enro.core.*
import dev.enro.core.compose.*
import dev.enro.core.compose.ComposableDestinationContextReference
import dev.enro.core.compose.getComposableDestinationContext
import dev.enro.core.container.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ComposableNavigationContainer internal constructor(
    override val id: String,
    override val parentContext: NavigationContext<*>,
    override val accept: (NavigationKey) -> Boolean,
    override val emptyBehavior: EmptyBehavior,
    internal val saveableStateHolder: SaveableStateHolder,
) : NavigationContainer {

    private val mutableBackstack: MutableStateFlow<NavigationContainerBackstack> =
        MutableStateFlow(createEmptyBackStack())
    override val backstackFlow: StateFlow<NavigationContainerBackstack> get() = mutableBackstack

    private val destinationStorage: ComposableContextStorage = parentContext.getComposableContextStorage()

    private val destinationContexts = destinationStorage.destinations.getOrPut(id) { mutableMapOf() }
    private val currentDestination get() = mutableBackstack.value.backstack
        .mapNotNull { destinationContexts[it.instructionId] }
        .lastOrNull {
            it.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
        }

    override val activeContext: NavigationContext<*>?
        get() = currentDestination?.destination?.navigationContext

    override fun setBackstack(backstack: NavigationContainerBackstack) {
        val lastBackstack = backstackFlow.value
        mutableBackstack.value = backstack

        val toRemoveEntries = lastBackstack.backstackEntries
            .filter {
                !backstack.backstackEntries.contains(it)
            }
        toRemoveEntries
            .mapNotNull {
                destinationContexts[it.instruction.instructionId]
            }
            .forEach {
                destinationContexts.remove(it.instruction.instructionId)
                it.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }

        if(backstack.lastInstruction is NavigationInstruction.Close) {
            parentContext.containerManager.setActiveContainerById(
                toRemoveEntries.firstOrNull()?.previouslyActiveContainerId
            )
        }
        else {
            parentContext.containerManager.setActiveContainer(this)
        }

        if(backstack.backstack.isEmpty()) {
            if(isActive) parentContext.containerManager.setActiveContainer(null)
            when(emptyBehavior) {
                EmptyBehavior.AllowEmpty -> {
                    /* If allow empty, pass through to default behavior */
                }
                EmptyBehavior.CloseParent -> {
                    parentContext.getNavigationHandle().close()
                    return
                }
                is EmptyBehavior.Action -> {
                    val consumed = emptyBehavior.onEmpty()
                    if (consumed) {
                        return
                    }
                }
            }
        }
    }

    internal fun onInstructionDisposed(instruction: NavigationInstruction.Open) {
        if (mutableBackstack.value.exiting == instruction) {
            mutableBackstack.value = mutableBackstack.value.copy(
                exiting = null,
                exitingIndex = -1
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

    @SuppressLint("ComposableNaming")
    @Composable
    internal fun bindDestination(instruction: NavigationInstruction.Open) {
        DisposableEffect(true) {
            onDispose {
                if (!mutableBackstack.value.backstack.contains(instruction)) {
                    destinationContexts.remove(instruction.instructionId)
                }
            }
        }
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