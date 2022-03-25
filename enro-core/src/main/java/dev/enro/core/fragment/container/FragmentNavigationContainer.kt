package dev.enro.core.fragment.container

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import dev.enro.core.*
import dev.enro.core.container.NavigationContainerBackstack
import dev.enro.core.container.createEmptyBackStack
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.isActive
import dev.enro.core.fragment.DefaultFragmentExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FragmentNavigationContainer(
    @IdRes val containerId: Int,
    private val parentContextFactory: () -> NavigationContext<*>,
    override val accept: (NavigationKey) -> Boolean,
    override val emptyBehavior: EmptyBehavior,
    internal val fragmentManager: () -> FragmentManager
) : NavigationContainer {
    override val id: String = containerId.toString()

    private val mutableBackstack: MutableStateFlow<NavigationContainerBackstack> =
        MutableStateFlow(createEmptyBackStack())
    override val backstackFlow: StateFlow<NavigationContainerBackstack> get() = mutableBackstack

    override val parentContext: NavigationContext<*>
        get() = parentContextFactory()

    override val activeContext: NavigationContext<*>?
        get() = fragmentManager().findFragmentById(containerId)?.navigationContext

    override fun setBackstack(backstack: NavigationContainerBackstack) {
        val lastBackstack = backstackFlow.value
        mutableBackstack.value = backstack

        val manager = fragmentManager()
        val toRemoveEntries = lastBackstack.backstackEntries
            .filter {
                !backstack.backstackEntries.contains(it)
            }
        val toRemove = toRemoveEntries
            .mapNotNull {
                manager.findFragmentByTag(it.instruction.instructionId)
            }
        val toDetach = backstack.backstack.dropLast(1)
            .mapNotNull {
                manager.findFragmentByTag(it.instructionId)
            }
        val activeInstruction = backstack.backstack.lastOrNull()
        val activeFragment = activeInstruction?.let {
            manager.findFragmentByTag(it.instructionId)
        }
        val newFragment = if(activeFragment == null && activeInstruction != null) {
            DefaultFragmentExecutor.createFragment(
                manager,
                parentContext.controller.navigatorForKeyType(activeInstruction.navigationKey::class)!!,
                activeInstruction
            )
        } else null

        manager.commitNow {
            if(backstack.lastInstruction is NavigationInstruction.Close) {
                parentContext.containerManager.setActiveContainerById(
                    toRemoveEntries.firstOrNull()?.previouslyActiveContainerId
                )
            }
            else {
                parentContext.containerManager.setActiveContainer(this@FragmentNavigationContainer)
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

            toRemove.forEach {
                remove(it)
            }
            toDetach.forEach {
                detach(it)
            }

            if(activeInstruction == null) return@commitNow

            if(activeFragment != null) {
                attach(activeFragment)
                setPrimaryNavigationFragment(activeFragment)
            }
            if(newFragment != null) {
                add(containerId, newFragment, activeInstruction.instructionId)
                setPrimaryNavigationFragment(newFragment)
            }
        }
    }
}
