package dev.enro.core.fragment.container

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import dev.enro.core.*
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.container.*
import dev.enro.core.fragment.FragmentNavigationBinding

public class FragmentPresentationContainer internal constructor(
    parentContext: NavigationContext<*>,
) : NavigationContainer(
    id = "FragmentPresentationContainer",
    parentContext = parentContext,
    acceptsNavigationKey = { true },
    emptyBehavior = EmptyBehavior.AllowEmpty,
    acceptsDirection = { it is NavigationDirection.Present },
    acceptsBinding = { it is FragmentNavigationBinding<*, *> || it is ComposableNavigationBinding<*, *> }
) {

    override var isVisible: Boolean = true

    override var currentAnimations: NavigationAnimation = DefaultAnimations.present
        private set

    override val activeContext: NavigationContext<out Fragment>?
        get() = backstackFlow.value.backstack
            .lastOrNull { fragmentManager.findFragmentByTag(it.instructionId) != null }
            ?.let {
                fragmentManager
                    .findFragmentByTag(it.instructionId)
                    ?.navigationContext
            }

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                if (f is DialogFragment && f.showsDialog) {
                    setBackstack(backstackFlow.value.close(f.tag ?: return))
                }
            }
        }, false)

        setOrLoadInitialBackstack(createEmptyBackStack())
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

        val toPresent = backstack.backstack
            .filter { fragmentManager.findFragmentByTag(it.instructionId) == null }
            .map {
                val binding =
                    parentContext.controller.bindingForKeyType(it.navigationKey::class)
                        ?: throw EnroException.UnreachableState()

                FragmentFactory.createFragment(
                    parentContext,
                    binding,
                    it
                ) to it
            }

        setAnimations(backstack)
        fragmentManager.commitNow {
            setReorderingAllowed(true)

            toRemove.forEach {
                remove(it.first)
            }

            toPresent.forEach {
                add(it.first, it.second.instructionId)
            }
        }

        backstack.backstack.lastOrNull()
            ?.let {
                fragmentManager.findFragmentByTag(it.instructionId)
            }
            ?.let { primaryFragment ->
                fragmentManager.commitNow {
                    setPrimaryNavigationFragment(primaryFragment)
                }
            }

        return true
    }

    private fun setAnimations(backstack: NavigationBackstack) {
        val previouslyActiveFragment =
            backstack.exiting?.let { fragmentManager.findFragmentByTag(it.instructionId) }
        currentAnimations = animationsFor(
            previouslyActiveFragment?.navigationContext ?: parentContext,
            backstack.lastInstruction
        )
    }
}