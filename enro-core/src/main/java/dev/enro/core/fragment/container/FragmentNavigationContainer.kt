package dev.enro.core.fragment.container

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import dev.enro.core.*
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.compose.dialog.animate
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.fragment.FragmentNavigator

class FragmentNavigationContainer internal constructor(
    @IdRes val containerId: Int,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    initialBackstack: NavigationBackstack
) : NavigationContainer(
    id = containerId.toString(),
    parentContext = parentContext,
    acceptsNavigationKey = accept,
    emptyBehavior = emptyBehavior,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward },
    acceptsNavigator = { it is FragmentNavigator<*, *>  || it is ComposableNavigator<*, *> }
) {
    override val activeContext: NavigationContext<*>?
        get() = fragmentManager.findFragmentById(containerId)?.navigationContext

    override var isVisible: Boolean
        get() {
            return containerView?.isVisible ?: false
        }
        set(value) {
            containerView?.isVisible = value
        }

    init {
        setOrLoadInitialBackstack(initialBackstack)
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstack: NavigationBackstack
    ): Boolean {
        if (!tryExecutePendingTransitions()) return false
        if (fragmentManager.isStateSaved) return false
        if (backstack != backstackFlow.value) return false

        val toRemove = removed
            .mapNotNull {
                val fragment = fragmentManager.findFragmentByTag(it.instructionId)
                when (fragment) {
                    null -> null
                    else -> fragment to it
                }
            }

        val toDetach = backstack.backstack
            .filter { it.navigationDirection !is NavigationDirection.Present }
            .filter { it != backstack.active }
            .mapNotNull { fragmentManager.findFragmentByTag(it.instructionId)?.to(it) }

        val activeInstruction = backstack.active
        val activeFragment = activeInstruction?.let {
            fragmentManager.findFragmentByTag(it.instructionId)
        }
        val newFragment = if (activeFragment == null && activeInstruction != null) {
            val navigator =
                parentContext.controller.navigatorForKeyType(activeInstruction.navigationKey::class)
                    ?: throw EnroException.UnreachableState()

            FragmentFactory.createFragment(
                parentContext,
                navigator,
                activeInstruction
            )
        } else null

        val activeIndex =
            backstack.renderable.indexOfFirst { it.instructionId == activeInstruction?.instructionId }
        activeFragment?.view?.z = 0f
        (toRemove + toDetach).forEach {
            val isBehindActiveFragment = backstack.renderable.indexOf(it.second) < activeIndex
            it.first.view?.z = when {
                isBehindActiveFragment -> -1f
                else -> 1f
            }
        }

        val primaryFragment = backstack.backstack.lastOrNull()
            ?.let {
                fragmentManager.findFragmentByTag(it.instructionId)
            }
            ?: newFragment

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
                }
                newFragment != null -> {
                    add(containerId, newFragment, activeInstruction.instructionId)
                }
            }
            if (primaryFragment != null) {
                setPrimaryNavigationFragment(primaryFragment)
            }
        }

        return true
    }
}

val FragmentNavigationContainer.containerView: View?
    get() {
        return when (parentContext.contextReference) {
            is Activity -> parentContext.contextReference.findViewById(containerId)
            is Fragment -> parentContext.contextReference.view?.findViewById(containerId)
            else -> null
        }
    }

fun FragmentNavigationContainer.setVisibilityAnimated(isVisible: Boolean) {
    val view = containerView ?: return
    if (!view.isVisible && !isVisible) return

    val animations = DefaultAnimations.present.asResource(view.context.theme)
    view.animate(
        animOrAnimator = when (isVisible) {
            true -> animations.enter
            false -> animations.exit
        },
        onAnimationStart = {
            view.translationZ = if (isVisible) 0f else -1f
            view.isVisible = true
        },
        onAnimationEnd = {
            view.isVisible = isVisible
        }
    )
}
