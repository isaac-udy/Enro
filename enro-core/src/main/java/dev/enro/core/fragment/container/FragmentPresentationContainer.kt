package dev.enro.core.fragment.container

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.*
import dev.enro.core.*
import dev.enro.core.compose.dialog.animate
import dev.enro.core.container.*
import dev.enro.core.container.close
import dev.enro.core.fragment.FragmentFactory
import dev.enro.core.fragment.internal.FullscreenDialogFragment

class FragmentPresentationContainer internal constructor(
    parentContext: NavigationContext<*>,
) : NavigationContainer(
    id = "FragmentPresentationContainer",
    parentContext = parentContext,
    accept = { true },
    emptyBehavior = EmptyBehavior.AllowEmpty,
    supportedNavigationDirections = setOf(NavigationDirection.Present)
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
        parentContext.runWhenContextActive {
            setBackstack(createEmptyBackStack())
        }

        fragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                if (f is DialogFragment && f.showsDialog) {
                    setBackstack(backstackFlow.value.close(f.tag ?: return))
                }
            }
        }, false)
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

        return true
    }
}