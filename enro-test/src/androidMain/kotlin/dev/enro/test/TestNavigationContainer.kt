package dev.enro.test

import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.NavigationController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class TestNavigationContainer(
    val key: NavigationContainerKey,
    backstack: NavigationBackstack,
) : NavigationContainerContext {
    private val mutableBackstackFlow = MutableStateFlow(backstack)
    override val backstackFlow: StateFlow<NavigationBackstack> = mutableBackstackFlow
    override val backstack: NavigationBackstack get() = backstackFlow.value

    override fun setBackstack(backstack: NavigationBackstack) {
        val backstackIds = backstack
            .map { it.instructionId }
            .toSet()

        TestNavigationHandle.allInstructions.apply {
            removeAll {
                it is NavigationInstruction.Open<*> && it.instructionId in backstackIds
            }
            addAll(backstack)
        }

        mutableBackstackFlow.value = backstack
    }

    override val isActive: Boolean = true
    override fun setActive() {}

    public override fun save(): SavedState {
        return savedState {
            putSavedStateList(BACKSTACK_KEY, backstack.map { encodeToSavedState(it, NavigationController.savedStateConfiguration) })
        }
    }

    public override fun restore(savedState: SavedState) {
        val restoredBackstack = savedState.read {
            getSavedStateListOrNull(BACKSTACK_KEY)
                .orEmpty()
                .map { decodeFromSavedState<AnyOpenInstruction>(it, NavigationController.savedStateConfiguration) }
                .toBackstack()
        }
        setBackstack(restoredBackstack)
    }

    companion object {
        private const val BACKSTACK_KEY = "TestNavigationContainer.BACKSTACK_KEY"
        val parentContainer = NavigationContainerKey.FromName("TestNavigationContainer.parentKey")
        val activeContainer = NavigationContainerKey.FromName("TestNavigationContainer.activeContainer")
    }
}

fun createTestNavigationContainer(
    key: NavigationContainerKey,
    backstack: NavigationBackstack = emptyBackstack(),
) = TestNavigationContainer(key, backstack)
