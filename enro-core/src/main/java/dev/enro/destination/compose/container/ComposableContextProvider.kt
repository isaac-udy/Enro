package dev.enro.destination.compose.container

import android.os.Bundle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.container.ComposableViewModelStoreStorage
import dev.enro.core.compose.container.getComposableViewModelStoreStorage
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.components.ContainerContextProvider
import dev.enro.core.container.components.ContainerState
import dev.enro.core.controller.get

internal class ComposableContextProvider(
    private val key: NavigationContainerKey,
    private val context: NavigationContext<*>,
) : ContainerContextProvider<ComposableDestination>, NavigationContainer.Component {

    private val viewModelStoreStorage: ComposableViewModelStoreStorage =
        context.getComposableViewModelStoreStorage()

    private val viewModelStores = viewModelStoreStorage.getStorageForContainer(key)

    private val restoredDestinationState = mutableMapOf<String, Bundle>()
    internal var destinationOwners by mutableStateOf<List<ComposableDestinationOwner>>(emptyList())

    private val currentDestination by derivedStateOf {
        destinationOwners
            .lastOrNull {
                it.instruction == state.backstack.active
            }
    }

    internal val childContext: NavigationContext<out ComposableDestination>? by derivedStateOf {
        currentDestination?.destination?.context
    }

    public val isAnimating: Boolean by derivedStateOf {
        destinationOwners.any { !it.transitionState.isIdle }
    }

    override fun getActiveNavigationContext(backstack: NavigationBackstack): NavigationContext<out ComposableDestination>? {
        return childContext
    }

    override fun getContext(instruction: AnyOpenInstruction): ComposableDestination? {
        return destinationOwners.firstOrNull { it.instruction.instructionId == instruction.instructionId }
            ?.destination
    }

    override fun createContext(instruction: AnyOpenInstruction): ComposableDestination {
        return createDestinationOwner(instruction).destination
    }

    lateinit var state: ContainerState

    override fun create(state: ContainerState) {
        this.state = state
    }

    public override fun save(): Bundle {
        val savedState = bundleOf()
        destinationOwners
            .filter { it.lifecycle.currentState != Lifecycle.State.DESTROYED }
            .forEach { destinationOwner ->
                savedState.putBundle(
                    DESTINATION_STATE_PREFIX_KEY + destinationOwner.instruction.instructionId,
                    destinationOwner.save()
                )
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

    override fun destroy() {
        destinationOwners.forEach { composableDestinationOwner ->
            composableDestinationOwner.destroy()
        }
        destinationOwners = emptyList()
    }

    private fun createDestinationOwner(instruction: AnyOpenInstruction): ComposableDestinationOwner {
        val controller = context.controller
        val composeKey = instruction.navigationKey
        val rawBinding = controller.bindingForKeyType(composeKey::class)
            ?: throw EnroException.MissingNavigationBinding(composeKey)

        if (rawBinding !is ComposableNavigationBinding<*, *>) {
            throw IllegalStateException("Expected ${composeKey::class.java.name} to be bound to a Composable, but was instead bound to a ${rawBinding.baseType.java.simpleName}")
        }
        val destination = rawBinding.constructDestination()

        val restoredState = restoredDestinationState.remove(instruction.instructionId)
        return ComposableDestinationOwner(
            parentContainer = context.containerManager.getContainer(key)!!,
            instruction = instruction,
            destination = destination,
            viewModelStore = viewModelStores.getOrPut(instruction.instructionId) { ViewModelStore() },
            onNavigationContextCreated = context.controller.dependencyScope.get(),
            onNavigationContextSaved = context.controller.dependencyScope.get(),
            composeEnvironment = context.controller.dependencyScope.get(),
            savedInstanceState = restoredState,
        )
    }

    private companion object {
        private const val DESTINATION_STATE_PREFIX_KEY = "DestinationState@"
    }
}