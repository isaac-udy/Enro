package dev.enro.fragment.container

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import dev.enro.core.*
import dev.enro.core.container.*
import dev.enro.fragment.FragmentContext

public class FragmentPresentationContainer internal constructor(
    parentContext: NavigationContext<*>,
) : NavigationContainer(
    id = "FragmentPresentationContainer",
    contextType = FragmentContext::class.java,
    parentContext = parentContext,
    acceptsNavigationKey = { true },
    emptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor = {},
    acceptsDirection = { it is NavigationDirection.Present },
    navigationHostFactory = parentContext.controller.dependencyScope.get()
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
        backstackState: NavigationBackstackState
    ): Boolean {
        if (!tryExecutePendingTransitions()) return false
        if (fragmentManager.isStateSaved) return false
        if (backstackState != backstackFlow.value) return false

        val toRemove = removed
            .mapNotNull {
                val fragment = fragmentManager.findFragmentByTag(it.instructionId)
                when (fragment) {
                    null -> null
                    else -> fragment to it
                }
            }

        val toPresent = backstackState.backstack
            .filter { fragmentManager.findFragmentByTag(it.instructionId) == null }
            .map { presentableInstruction ->
                FragmentFactory.createFragment(
                    parentContext,
                    presentableInstruction
                ) to presentableInstruction
            }

        setAnimations(backstackState)
        fragmentManager.commitNow {
            setReorderingAllowed(true)

            toRemove.forEach {
                remove(it.first)
            }

            toPresent.forEach {
                add(it.first, it.second.instructionId)
            }
        }

        backstackState.backstack.lastOrNull()
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

    private fun setAnimations(backstackState: NavigationBackstackState) {
        val previouslyActiveFragment =
            backstackState.exiting?.let { fragmentManager.findFragmentByTag(it.instructionId) }
        currentAnimations = animationsFor(
            previouslyActiveFragment?.navigationContext ?: parentContext,
            backstackState.lastInstruction
        )
    }
}