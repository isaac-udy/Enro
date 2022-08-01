package dev.enro.core.fragment.container

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.*
import dev.enro.core.*
import dev.enro.core.compose.dialog.animate
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.close
import dev.enro.core.fragment.FragmentFactory
import dev.enro.core.fragment.internal.FullscreenDialogFragment

class FragmentNavigationContainer internal constructor(
    @IdRes val containerId: Int,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior
) : NavigationContainer(
    id = containerId.toString(),
    parentContext = parentContext,
    accept = accept,
    emptyBehavior = emptyBehavior,
) {
    private val fragmentManager = when(parentContext.contextReference) {
        is FragmentActivity -> parentContext.contextReference.supportFragmentManager
        is Fragment -> parentContext.contextReference.childFragmentManager
        else -> throw IllegalStateException("Expected Fragment or FragmentActivity, but was ${parentContext.contextReference}")
    }

    override val activeContext: NavigationContext<*>?
        get() = fragmentManager.findFragmentById(containerId)?.navigationContext

    override var isVisible: Boolean
        get() {
            if(id == PRESENTATION_CONTAINER) return true
            return containerView?.isVisible ?: false
        }
        set(value) {
            if(id == PRESENTATION_CONTAINER) return
            containerView?.isVisible = value
        }

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(object: FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                if(f is DialogFragment && f.showsDialog) {
                    setBackstack(backstackFlow.value.close(f.tag ?: return))
                }
            }
        }, false)
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstack: NavigationBackstack
    ): Boolean {
        if(!tryExecutePendingTransitions() || fragmentManager.isStateSaved || backstack != backstackFlow.value){
            return false
        }

        val toRemove = removed
            .mapNotNull {
                val fragment = fragmentManager.findFragmentByTag(it.instructionId)
                when(fragment) {
                    null -> null
                    else -> fragment to it
                }
            }

        val toDetach = backstack.backstack
            .filter { it.navigationDirection !is NavigationDirection.Present }
            .filter { it != backstack.active }
            .mapNotNull { fragmentManager.findFragmentByTag(it.instructionId)?.to(it) }

        val toPresent = backstack.backstack
            .filter {
                it.navigationDirection is NavigationDirection.Present
            }
            .filter { fragmentManager.findFragmentByTag(it.instructionId) == null }
            .map {
                val navigator = parentContext.controller.navigatorForKeyType(it.navigationKey::class)
                    ?: throw EnroException.UnreachableState()

                FragmentFactory.createFragment(
                    parentContext,
                    navigator,
                    it
                ) to it
            }
            .map {
                if(it.second.navigationDirection is NavigationDirection.Present && it.first !is DialogFragment) {
                    FullscreenDialogFragment().apply { fragment = it.first } to it.second
                } else it
            }


        val activeInstruction = backstack.active
        val activeFragment = activeInstruction?.let {
            fragmentManager.findFragmentByTag(it.instructionId)
        }
        val newFragment = if(activeFragment == null && activeInstruction != null) {
            val navigator = parentContext.controller.navigatorForKeyType(activeInstruction.navigationKey::class)
                ?: throw EnroException.UnreachableState()

            FragmentFactory.createFragment(
                parentContext,
                navigator,
                activeInstruction
            )
        } else null

        val activeIndex = backstack.renderable.indexOfFirst { it.instructionId == activeInstruction?.instructionId }
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
                    ?: toPresent.firstOrNull { presented -> presented.second.instructionId == it.instructionId }?.first
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

            toPresent.forEach {
                add(it.first, it.second.instructionId)
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
            if(primaryFragment != null) {
                setPrimaryNavigationFragment(primaryFragment)
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

val FragmentNavigationContainer.containerView: View?
    get() {
        return when(parentContext.contextReference) {
            is Activity -> parentContext.contextReference.findViewById(containerId)
            is Fragment -> parentContext.contextReference.view?.findViewById(containerId)
            else -> null
        }
    }

fun FragmentNavigationContainer.setVisibilityAnimated(isVisible: Boolean) {
    val view = containerView ?: return
    if(!view.isVisible && !isVisible) return

    val animations = DefaultAnimations.present.asResource(view.context.theme)
    view.animate(
        animOrAnimator = when(isVisible) {
            true -> animations.enter
            false -> animations.exit
        },
        onAnimationStart = {
            view.translationZ = if(isVisible) 0f else -1f
            view.isVisible = true
        },
        onAnimationEnd = {
            view.isVisible = isVisible
        }
    )
}
