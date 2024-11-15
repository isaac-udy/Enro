package dev.enro.test

import android.os.Bundle
import androidx.core.os.bundleOf
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.container.emptyBackstack
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

    override fun save(): Bundle {
        return bundleOf(
            BACKSTACK_KEY to ArrayList(backstack)
        )
    }

    override fun restore(bundle: Bundle) {
        TODO("Not yet implemented")
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
