package dev.enro.destination.compose.container

import android.os.Bundle
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.activity
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.components.ContainerRenderer
import dev.enro.core.container.components.ContainerState
import dev.enro.core.container.components.getOrCreateContext
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.merge

internal class ComposableContainerRenderer(
    private val key: NavigationContainerKey,
    private val contextProvider: ComposableContextProvider,
    private val context: NavigationContext<*>,
) : ContainerRenderer, NavigationContainer.Component {

    override val isVisible: Boolean
        get() = true

    private var state: ContainerState? = null

    // We want "Render" to look like it's a Composable function (it's a Composable lambda), so
    // we are uppercasing the first letter of the property name, which triggers a PropertyName lint warning
    @Suppress("PropertyName")
    public val Render: @Composable () -> Unit = movableContentOf {
        key(key.name) {
            val backstack = state?.backstack ?: emptyBackstack()
            val result = remember(state!!.currentTransition) {
                onBackstackUpdated(state!!.currentTransition)
            }
            val destinationOwners = remember(backstack) {
                backstack.map {
                    contextProvider.getOrCreateContext(it)
                        .owner
                }
            }

            destinationOwners
                .forEach {
                    key(it.instruction.instructionId) {
                        it.Render(backstack)
                    }
                }
        }
    }

    override fun create(state: ContainerState) {
        this.state = state
    }

    override fun save(): Bundle {
        return super.save()
    }

    override fun restore(bundle: Bundle) {
        super.restore(bundle)
    }

    override fun destroy() {
        super.destroy()
    }

    @OptIn(ExperimentalMaterialApi::class)
    private fun onBackstackUpdated(
        transition: NavigationBackstackTransition
    ): Boolean {
        if (!context.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) return false
        if (context.runCatching { context.activity }.getOrNull() == null) return false
        val state = state ?: return false

        val activeDestinations = contextProvider.destinationOwners
            .filter {
                it.lifecycle.currentState != Lifecycle.State.DESTROYED
            }
            .associateBy { it.instruction }
            .toMutableMap()

        transition.removed
            .mapNotNull { activeDestinations[it]?.destination }
            .forEach {
                when (it) {
                    is DialogDestination -> it.dialogConfiguration.isDismissed.value = true
                    is BottomSheetDestination -> it.bottomSheetConfiguration.isDismissed.value =
                        true
                }
            }

        state.backstack.forEach { instruction ->
            if (activeDestinations[instruction] == null) {
                activeDestinations[instruction] = contextProvider.createContext(instruction).owner
            }
        }

        val visible = mutableSetOf<AnyOpenInstruction>()

        state.backstack.takeLastWhile { it.navigationDirection == NavigationDirection.Present }
            .forEach { visible.add(it) }

        state.backstack.lastOrNull { it.navigationDirection == NavigationDirection.Push }
            ?.let { visible.add(it) }

        contextProvider.destinationOwners.forEach {
            if (activeDestinations[it.instruction] == null) {
                it.transitionState.targetState = false
            }
        }
        contextProvider.destinationOwners = merge(transition.previousBackstack, transition.activeBackstack)
            .mapNotNull { instruction ->
                activeDestinations[instruction]
            }
        setVisibilityForBackstack(transition)
        return true
    }

    @OptIn(ExperimentalMaterialApi::class)
    internal fun setVisibilityForBackstack(transition: NavigationBackstackTransition) {
        val isParentContextStarted =
            context.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        // TODO: !isParentContextStarted && shouldTakeAnimationsFromParentContainer
        if (!isParentContextStarted && false) return

        val isParentBeingRemoved = when {
            context.contextReference is Fragment && !context.contextReference.isAdded -> true
            else -> false
        }
        val presented =
            transition.activeBackstack.takeLastWhile { it.navigationDirection is NavigationDirection.Present }
                .toSet()
        val activePush =
            transition.activeBackstack.lastOrNull { it.navigationDirection !is NavigationDirection.Present }
        val activePresented = presented.lastOrNull()

        state?.backstack.orEmpty()
            .mapNotNull { contextProvider.getContext(it) }
            .map { it.owner }
            .forEach { destinationOwner ->
            val instruction = destinationOwner.instruction
            val isPushedDialogOrBottomSheet =
                ((destinationOwner.destination is DialogDestination || destinationOwner.destination is BottomSheetDestination) && activePresented != null)

            destinationOwner.transitionState.targetState = when (instruction) {
                activePresented -> !isParentBeingRemoved
                activePush -> !isParentBeingRemoved && !isPushedDialogOrBottomSheet
                else -> false
            }
        }
    }
}