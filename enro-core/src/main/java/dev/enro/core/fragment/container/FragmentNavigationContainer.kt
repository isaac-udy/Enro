package dev.enro.core.fragment.container

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import dev.enro.core.*
import dev.enro.core.container.*
import dev.enro.core.fragment.DefaultFragmentExecutor

class FragmentNavigationContainer internal constructor(
    @IdRes val containerId: Int,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    val fragmentManager: FragmentManager
) : NavigationContainer(
    id = containerId.toString(),
    parentContext = parentContext,
    accept = accept,
    emptyBehavior = emptyBehavior,
) {
    override val activeContext: NavigationContext<*>?
        get() = fragmentManager.findFragmentById(containerId)?.navigationContext

    override val isVisible: Boolean
        get() = when(parentContext.contextReference) {
            is Activity -> parentContext.contextReference.findViewById<View>(containerId).isVisible
            is Fragment -> parentContext.contextReference.view?.findViewById<View>(containerId)?.isVisible ?: false
            else -> false
        }

    override fun reconcileBackstack(
        removed: List<OpenPushInstruction>,
        backstack: NavigationContainerBackstack
    ): Boolean {
        if(!tryExecutePendingTransitions() || fragmentManager.isStateSaved){
            return false
        }

        val toRemove = removed
            .mapNotNull {
                fragmentManager.findFragmentByTag(it.instructionId)?.to(it)
            }

        val toDetach = backstack.backstack.dropLast(1)
            .mapNotNull {
                fragmentManager.findFragmentByTag(it.instructionId)?.to(it)
            }

        val activeInstruction = backstack.visible
        val activeFragment = activeInstruction?.let {
            fragmentManager.findFragmentByTag(it.instructionId)
        }
        val newFragment = if(activeFragment == null && activeInstruction != null) {
            val navigator = parentContext.controller.navigatorForKeyType(activeInstruction.navigationKey::class)
                ?: throw EnroException.UnreachableState()

            DefaultFragmentExecutor.createFragment(
                fragmentManager,
                navigator,
                activeInstruction
            )
        } else null

        val activeIndex = backstack.renderable.indexOf(activeInstruction)
        activeFragment?.view?.z = 0f
        (toRemove + toDetach).forEach {
            val isBehindActiveFragment = backstack.renderable.indexOf(it.second) < activeIndex
            it.first.view?.z = when {
                isBehindActiveFragment -> -1f
                else -> 1f
            }
        }
        fragmentManager.commitNow {
            if (!backstack.isDirectUpdate) {
                val animations = animationsFor(parentContext, backstack.lastInstruction)
                setCustomAnimations(animations.enter, animations.exit)
            }

            toRemove.forEach {
                remove(it.first)
            }

            toDetach.forEach {
                detach(it.first)
            }

            when {
                activeInstruction == null -> { /* Pass */ }
                activeFragment != null -> {
                    attach(activeFragment)
                    setPrimaryNavigationFragment(activeFragment)
                }
                newFragment != null -> {
                    add(containerId, newFragment, activeInstruction.instructionId)
                    setPrimaryNavigationFragment(newFragment)
                }
            }
        }

        return true
    }

    private fun tryExecutePendingTransitions(): Boolean {
        return kotlin
            .runCatching {
                fragmentManager.executePendingTransactions()
                true
            }
            .getOrDefault(false)
    }

}
