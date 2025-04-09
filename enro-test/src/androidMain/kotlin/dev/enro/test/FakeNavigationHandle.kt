@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.backstackOf
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.internal.handle.NavigationHandleScope

/**
 * FakeNavigationHandle is a NavigationHandle which does not perform any navigation instructions,
 * instead it records them in a list that can be later used to perform assertions.
 */
internal class FakeNavigationHandle(
    key: NavigationKey,
    private val onCloseRequested: () -> Unit,
): NavigationHandle {
    override val instruction: NavigationInstruction.Open<*> =
        NavigationInstruction.Open.OpenInternal(
            navigationDirection = when (key) {
                is NavigationKey.SupportsPush -> NavigationDirection.Push
                is NavigationKey.SupportsPresent -> NavigationDirection.Present
                else -> NavigationDirection.Push
            },
            navigationKey = key
        )
    private val instructions = mutableListOf<NavigationInstruction>()

    internal val navigationContainers = mutableMapOf<NavigationContainerKey, TestNavigationContainer>(
        TestNavigationContainer.parentContainer to createTestNavigationContainer(
            key = TestNavigationContainer.parentContainer,
            backstack = backstackOf(instruction)
        ),
        TestNavigationContainer.activeContainer to createTestNavigationContainer(
            TestNavigationContainer.activeContainer
        )
    )


    @SuppressLint("VisibleForTests")
    override val lifecycle: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
        currentState = Lifecycle.State.RESUMED
    }

    override val id: String = instruction.instructionId
    override val key: NavigationKey = key
    override val dependencyScope: EnroDependencyScope = NavigationHandleScope(
        navigationController = EnroTest.getCurrentNavigationController(),
        savedStateHandle = SavedStateHandle(),
    ).bind(this)

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        instructions.add(navigationInstruction)
        when (navigationInstruction) {
            is NavigationInstruction.RequestClose -> {
                onCloseRequested()
            }
            is NavigationInstruction.ContainerOperation -> {
                val containerKey = when (val target = navigationInstruction.target) {
                    NavigationInstruction.ContainerOperation.Target.ParentContainer -> TestNavigationContainer.parentContainer
                    NavigationInstruction.ContainerOperation.Target.ActiveContainer -> TestNavigationContainer.activeContainer
                    is NavigationInstruction.ContainerOperation.Target.TargetContainer -> target.key
                }
                val container = navigationContainers[containerKey]
                    ?: throw IllegalStateException("TestNavigationHandle was not configured to have container with key $containerKey")
                container.apply(navigationInstruction.operation)
            }
            else -> {}
        }
    }
}