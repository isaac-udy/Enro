package dev.enro.core.fragment.container

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.*
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstackState
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.close
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.extensions.animate

public class FragmentNavigationContainer internal constructor(
    @IdRes public val containerId: Int,
    key: NavigationContainerKey = NavigationContainerKey.FromId(containerId),
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    initialBackstackState: NavigationBackstackState
) : NavigationContainer(
    key = key,
    parentContext = parentContext,
    contextType = Fragment::class.java,
    acceptsNavigationKey = accept,
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward || it is NavigationDirection.Present },
) {
    private val hostInstructionAs = parentContext.controller.dependencyScope.get<HostInstructionAs>()

    override var isVisible: Boolean
        get() {
            return containerView?.isVisible ?: false
        }
        set(value) {
            containerView?.isVisible = value
        }

    override val activeContext: NavigationContext<out Fragment>?
        get() {
            val fragment =  backstackState.active?.let { fragmentManager.findFragmentByTag(it.instructionId) }
                ?: fragmentManager.findFragmentById(containerId)
            return fragment?.navigationContext
        }

    override var currentAnimations: NavigationAnimation = DefaultAnimations.none
        private set

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f !is DialogFragment) return
                val instructionId = f.tag ?: return
                if (fm.isDestroyed || fm.isStateSaved) return
                if (!f.isRemoving) return
                setBackstack(backstackState.close(instructionId))
            }
        }, false)
        setOrLoadInitialBackstack(initialBackstackState)
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstackState: NavigationBackstackState
    ): Boolean {
        if (!tryExecutePendingTransitions()) return false
        if (fragmentManager.isStateSaved) return false
        if (backstackState != backstackFlow.value) return false

        val toRemove = removed.asFragmentAndInstruction()
        val toDetach = getFragmentsToDetach(backstackState)
        val toPresent = getFragmentsToPresent(backstackState)
        val activePushed = getActivePushedFragment(backstackState)

        (toRemove + toDetach + activePushed)
            .filterNotNull()
            .forEach {
                setZIndexForAnimations(backstackState, it)
            }

        setAnimations(backstackState)
        if(
            toRemove.isEmpty()
            && toDetach.isEmpty()
            && toPresent.isEmpty()
            && (fragmentManager.primaryNavigationFragment == activePushed?.fragment || activePushed == null)
        ) return true

        fragmentManager.commitNow {
            setReorderingAllowed(true)
            applyAnimationsForTransaction(
                backstackState = backstackState,
                active = activePushed
            )

            toRemove.forEach {
                when {
                    it.fragment is DialogFragment && it.fragment.showsDialog -> it.fragment.dismiss()
                    else -> remove(it.fragment)
                }
            }
            toDetach.forEach {
                detach(it.fragment)
            }
            toPresent.forEach {
                if(!it.fragment.isAdded) {
                    if(it.fragment.isDetached) {
                        attach(it.fragment)
                    } else {
                        add(it.fragment, it.instruction.instructionId)
                    }
                }

            }
            if (activePushed != null) {
                when {
                    activePushed.fragment.id != 0 -> attach(activePushed.fragment)
                    else -> add(
                        containerId,
                        activePushed.fragment,
                        activePushed.instruction.instructionId
                    )
                }
            }
            val activeFragmentAndInstruction = toPresent.lastOrNull() ?: activePushed ?: return@commitNow
            val activeFragment = activeFragmentAndInstruction.fragment
            setPrimaryNavigationFragment(activeFragment)
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

    private fun List<AnyOpenInstruction>.asFragmentAndInstruction(): List<FragmentAndInstruction> {
        return mapNotNull { instruction ->
            val fragment = fragmentManager.findFragmentByTag(instruction.instructionId) ?: return@mapNotNull null
            FragmentAndInstruction(
                fragment = fragment,
                instruction = instruction
            )
        }
    }

    private fun getFragmentsToDetach(backstackState: NavigationBackstackState): List<FragmentAndInstruction> {
        return backstackState.backstack
            .dropLastWhile { it.navigationDirection is NavigationDirection.Present }
            .dropLast(1)
            .asFragmentAndInstruction()
    }

    private fun getFragmentsToPresent(backstackState: NavigationBackstackState) : List<FragmentAndInstruction> {
        return backstackState.backstack
            .takeLastWhile {
                it.navigationDirection is NavigationDirection.Present
            }
            .map { getOrCreateFragment(DialogFragment::class.java, it)  }
    }

    private fun getActivePushedFragment(backstackState: NavigationBackstackState) : FragmentAndInstruction? {
        val activePushedFragment = backstackState.backstack
            .lastOrNull {
                it.navigationDirection is NavigationDirection.Push
            } ?: return null
        return getOrCreateFragment(Fragment::class.java, activePushedFragment)
    }

    private fun getOrCreateFragment(type: Class<out Fragment>, instruction: AnyOpenInstruction): FragmentAndInstruction {
        val existingFragment = fragmentManager.findFragmentByTag(instruction.instructionId)
        if (existingFragment != null) return FragmentAndInstruction(
            fragment = existingFragment,
            instruction = instruction
        )

        return FragmentAndInstruction(
            fragment = FragmentFactory.createFragment(
                parentContext,
                hostInstructionAs(type, parentContext, instruction)
            ),
            instruction = instruction
        )
    }

    private fun setZIndexForAnimations(backstack: NavigationBackstackState, fragmentAndInstruction: FragmentAndInstruction) {
        val activeIndex =
            backstack.renderable.indexOfFirst { it.instructionId == backstack.active?.instructionId }
        val index = backstack.renderable.indexOf(fragmentAndInstruction.instruction)

        fragmentAndInstruction.fragment.view?.z = when {
            index == activeIndex -> 0f
            index < activeIndex -> -1f
            else -> 1f
        }
    }

    private fun setAnimations(backstackState: NavigationBackstackState) {
        val shouldTakeAnimationsFromParentContainer = parentContext is FragmentContext<out Fragment>
                && parentContext.contextReference is NavigationHost
                && backstackState.backstack.size <= 1

        val previouslyActiveFragment = fragmentManager.findFragmentById(containerId)
        val previouslyActiveContext = runCatching { previouslyActiveFragment?.navigationContext }.getOrNull()
        currentAnimations = when {
            backstackState.isRestoredState -> DefaultAnimations.none
            shouldTakeAnimationsFromParentContainer -> parentContext.parentContainer()!!.currentAnimations
            backstackState.isInitialState -> DefaultAnimations.none
            else -> animationsFor(
                previouslyActiveContext ?: parentContext,
                backstackState.lastInstruction
            )
        }
    }

    private fun FragmentTransaction.applyAnimationsForTransaction(
        backstackState: NavigationBackstackState,
        active: FragmentAndInstruction?
    ) {
        if (backstackState.isRestoredState) return
        val previouslyActiveFragment = fragmentManager.findFragmentById(containerId)
        val resourceAnimations = currentAnimations.asResource(parentContext.activity.theme)

        setCustomAnimations(
            if (active?.fragment is NavigationHost) R.anim.enro_no_op_enter_animation else resourceAnimations.enter,
            if (previouslyActiveFragment is NavigationHost) R.anim.enro_no_op_exit_animation else resourceAnimations.exit
        )
    }
}

private data class FragmentAndInstruction(
    val fragment: Fragment,
    val instruction: AnyOpenInstruction
)

public val FragmentNavigationContainer.containerView: View?
    get() {
        return when (parentContext.contextReference) {
            is Activity -> parentContext.contextReference.findViewById(containerId)
            is Fragment -> parentContext.contextReference.view?.findViewById(containerId)
            else -> null
        }
    }

public fun FragmentNavigationContainer.setVisibilityAnimated(
    isVisible: Boolean,
    animations: NavigationAnimation = DefaultAnimations.present
) {
    val view = containerView ?: return
    if (!view.isVisible && !isVisible) return
    if (view.isVisible && isVisible) return

    val resourceAnimations = animations.asResource(view.context.theme)
    view.animate(
        animOrAnimator = when (isVisible) {
            true -> resourceAnimations.enter
            false -> resourceAnimations.exit
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
