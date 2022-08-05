package dev.enro.core.fragment.container

import androidx.fragment.app.*
import dev.enro.core.*
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.container.*
import dev.enro.core.container.close
import dev.enro.core.fragment.FragmentFactory
import dev.enro.core.fragment.FragmentNavigator
import dev.enro.core.fragment.internal.FullscreenDialogFragment

class FragmentPresentationContainer internal constructor(
    parentContext: NavigationContext<*>,
) : NavigationContainer(
    id = "FragmentPresentationContainer",
    parentContext = parentContext,
    acceptsNavigationKey = { true },
    emptyBehavior = EmptyBehavior.AllowEmpty,
    acceptsDirection = { it is NavigationDirection.Present },
    acceptsNavigator = { it is FragmentNavigator<*, *> || it is ComposableNavigator<*, *> }
) {

    override var isVisible: Boolean = true

    override val activeContext: NavigationContext<*>?
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
                val navigator =
                    parentContext.controller.navigatorForKeyType(it.navigationKey::class)
                        ?: throw EnroException.UnreachableState()

                FragmentFactory.createFragment(
                    parentContext,
                    navigator,
                    it
                ) to it
            }
            .map {
                if (it.first !is DialogFragment) {
                    FullscreenDialogFragment().apply { fragment = it.first } to it.second
                } else it
            }

        fragmentManager.commitNow {
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
}