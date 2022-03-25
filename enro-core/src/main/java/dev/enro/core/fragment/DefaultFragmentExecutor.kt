package dev.enro.core.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.*
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.fragment.container.FragmentNavigationContainer
import dev.enro.core.fragment.internal.SingleFragmentKey

private const val PREVIOUS_FRAGMENT_IN_CONTAINER = "dev.enro.core.fragment.DefaultFragmentExecutor.PREVIOUS_FRAGMENT_IN_CONTAINER"

object DefaultFragmentExecutor : NavigationExecutor<Any, Fragment, NavigationKey>(
    fromType = Any::class,
    opensType = Fragment::class,
    keyType = NavigationKey::class
) {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun open(args: ExecutorArgs<out Any, out Fragment, out NavigationKey>) {
        val fromContext = args.fromContext
        val navigator = args.navigator as FragmentNavigator
        val instruction = args.instruction

        val containerManager = args.fromContext.containerManager
        val host = containerManager.activeContainer?.takeIf { it.accept(args.key) }
            ?: args.fromContext.containerManager.containers
            .filterIsInstance<FragmentNavigationContainer>()
            .firstOrNull { it.accept(args.key) }

        if (!tryExecutePendingTransitions(navigator, fromContext, instruction)) return
        if (fromContext is FragmentContext && !fromContext.fragment.isAdded) return
        if (host == null) {
            val parentContext = fromContext.parentContext()
            if(parentContext == null) {
                openFragmentAsActivity(fromContext, instruction)
            }
            else {
                open(
                    ExecutorArgs(
                        parentContext,
                        navigator,
                        instruction.navigationKey,
                        instruction
                    )
                )
            }
            return
        }

        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        val fragmentActivity = fromContext.activity
        if (fragmentActivity !is FragmentActivity) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        if (instruction.navigationDirection == NavigationDirection.REPLACE && fromContext.contextReference is FragmentActivity) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        if(instruction.navigationDirection == NavigationDirection.REPLACE && fromContext.contextReference is ComposableDestination) {
            TODO()
//            fromContext.contextReference.contextReference.requireParentContainer().close()
        }

        val isDialog = DialogFragment::class.java.isAssignableFrom(args.navigator.contextType.java)

        if(isDialog) {
            val fragment = createFragment(
                fragmentActivity.supportFragmentManager,
                navigator,
                instruction,
            ) as DialogFragment

            if(fromContext.contextReference is DialogFragment) {
                if (instruction.navigationDirection == NavigationDirection.REPLACE) {
                    fromContext.contextReference.dismiss()
                }

                fragment.show(
                    fragmentActivity.supportFragmentManager,
                    instruction.instructionId
                )
            }
            else {
                fragment.show(fragmentActivity.supportFragmentManager, instruction.instructionId)
            }
            return
        }

        host.setBackstack(
            host.backstackFlow.value.push(
                args.instruction,
                args.fromContext.containerManager.activeContainer?.id
            )
        )
    }


    override fun close(context: NavigationContext<out Fragment>) {
        val container = context.parentContext()?.containerManager?.containers?.firstOrNull { it.activeContext == context }
        if(container == null) {
            context.contextReference.parentFragmentManager.commitNow {
                remove(context.contextReference)
            }
            return
        }

        container.setBackstack(
            container.backstackFlow.value.close(
                context.getNavigationHandle().id
            )
        )
    }

    fun createFragment(
        fragmentManager: FragmentManager,
        navigator: Navigator<*, *>,
        instruction: NavigationInstruction.Open
    ): Fragment {
        val fragment = fragmentManager.fragmentFactory.instantiate(
            navigator.contextType.java.classLoader!!,
            navigator.contextType.java.name
        )

        fragment.arguments = Bundle()
            .addOpenInstruction(instruction)

        return fragment
    }

    private fun tryExecutePendingTransitions(
        navigator: FragmentNavigator<*, *>,
        fromContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Open
    ): Boolean {
        return kotlin
            .runCatching {
                if (fromContext.contextReference is Fragment) {
                    fromContext.contextReference.parentFragmentManager.executePendingTransactions()
                    fromContext.contextReference.childFragmentManager.executePendingTransactions()
                }
                true
            }
            .onFailure {
                mainThreadHandler.post {
                    open(ExecutorArgs(fromContext, navigator, instruction.navigationKey, instruction))
                }
            }
            .getOrDefault(false)
    }

    //        val fromContext = args.fromContext
//        val navigator = args.navigator
//        val instruction = args.instruction
//
//        navigator as FragmentNavigator<*, *>
//
//        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
//            openFragmentAsActivity(fromContext, instruction)
//            return
//        }
//
//        if (instruction.navigationDirection == NavigationDirection.REPLACE && fromContext.contextReference is FragmentActivity) {
//            openFragmentAsActivity(fromContext, instruction)
//            return
//        }
//
//        if(instruction.navigationDirection == NavigationDirection.REPLACE && fromContext.contextReference is ComposableDestination) {
//            fromContext.contextReference.contextReference.requireParentContainer().close()
//        }
//
//        if (!tryExecutePendingTransitions(navigator, fromContext, instruction)) return
//        if (fromContext is FragmentContext && !fromContext.fragment.isAdded) return
//        val fragment = createFragment(
//            fromContext.childFragmentManager,
//            navigator,
//            instruction
//        )
//
//        if(fragment is DialogFragment) {
//            if(fromContext.contextReference is DialogFragment) {
//                if (instruction.navigationDirection == NavigationDirection.REPLACE) {
//                    fromContext.contextReference.dismiss()
//                }
//
//                fragment.show(
//                    fromContext.contextReference.parentFragmentManager,
//                    instruction.instructionId
//                )
//            }
//            else {
//                fragment.show(fromContext.childFragmentManager, instruction.instructionId)
//            }
//            return
//        }
//
//        val host = fromContext.fragmentHostFor(instruction)
//        if (host == null) {
//            openFragmentAsActivity(fromContext, instruction)
//            return
//        }
//
//        val activeFragment = host.fragmentManager.findFragmentById(host.containerId)
//        activeFragment?.view?.let {
//            ViewCompat.setZ(it, -1.0f)
//        }
//
//        val animations = animationsFor(fromContext, instruction)
//
//        host.fragmentManager.commitNow {
//            addSharedElementsToOpenTransaction(args, fragment)
//            setCustomAnimations(animations.enter, animations.exit)
//
//            if(fromContext.contextReference is DialogFragment && instruction.navigationDirection == NavigationDirection.REPLACE) {
//                fromContext.contextReference.dismiss()
//            }
//
//            if(activeFragment != null) {
//                if (instruction.navigationDirection == NavigationDirection.FORWARD) {
//                    host.fragmentManager.putFragment(
//                        instruction.internal.additionalData,
//                        PREVIOUS_FRAGMENT_IN_CONTAINER,
//                        activeFragment
//                    )
//                    detach(activeFragment)
//                }
//                if (instruction.navigationDirection == NavigationDirection.REPLACE) {
//                    val activeFragmentPreviousFragment =
//                        host.fragmentManager.getFragment(activeFragment.getNavigationHandleViewModel().instruction.additionalData, PREVIOUS_FRAGMENT_IN_CONTAINER)
//
//                    if(activeFragmentPreviousFragment != null) {
//                        host.fragmentManager.putFragment(
//                            instruction.internal.additionalData,
//                            PREVIOUS_FRAGMENT_IN_CONTAINER,
//                            activeFragmentPreviousFragment
//                        )
//                    }
//                }
//            }
//            replace(host.containerId, fragment, instruction.instructionId)
//            setPrimaryNavigationFragment(fragment)
//        }
    }

    private fun FragmentTransaction.addSharedElementsToOpenTransaction(
        args: ExecutorArgs<out Any, out Fragment, out NavigationKey>,
        fragment: Fragment
    ) {
//        val fromContext = args.fromContext
//        val instruction = args.instruction
//        val elements = instruction.getSharedElements()
//        if(elements.isEmpty()) return
//
//        fragment.postponeEnterTransition()
//        if(fromContext.contextReference is Fragment) {
//            elements
//                .also {
//                    if(it.isNotEmpty()) {
//                        fragment.sharedElementEnterTransition = AutoTransition()
//                        fragment.sharedElementReturnTransition = AutoTransition()
//                    }
//                }
//                .forEach {
//                    val view = fromContext.contextReference.requireView()
//                        .findViewById<View>(it.from)
//                    view.transitionName = it.transitionName
//
//                    addSharedElement(view, view.transitionName)
//                }
//        }
//
//        runOnCommit {
//            elements
//                .forEach {
//                    fragment.requireView()
//                        .findViewById<View>(it.opens)
//                        .transitionName = it.transitionName
//                }
//            fragment.startPostponedEnterTransition()
//        }
    }

//    override fun close(context: NavigationContext<out Fragment>) {}
//        if (context.contextReference is DialogFragment) {
//            context.contextReference.dismiss()
//            return
//        }
//
//        val previousFragmentInContainer = runCatching {
//            context.fragment.parentFragmentManager.getFragment(
//                context.getNavigationHandleViewModel().instruction.additionalData,
//                PREVIOUS_FRAGMENT_IN_CONTAINER
//            )
//        }.getOrNull()
//
//        val containerWillBeEmpty = previousFragmentInContainer == null
//        if (containerWillBeEmpty) {
//            val container = context.parentContext()
//                ?.getNavigationHandleViewModel()
//                ?.childContainers
//                ?.firstOrNull {
//                    it.containerId == context.contextReference.id
//                }
//            if(container != null) {
//                when(container.emptyBehavior) {
//                    EmptyBehavior.AllowEmpty -> { /* continue */ }
//                    EmptyBehavior.CloseParent -> {
//                        context.parentContext()?.getNavigationHandle()?.close()
//                        return
//                    }
//                    is EmptyBehavior.Action -> {
//                        val consumed = container.emptyBehavior.onEmpty()
//                        if (consumed) {
//                            return
//                        }
//                    }
//                }
//            }
//        }
//
//        val previousFragment = context.getPreviousFragment()
//        val animations = animationsFor(context, NavigationInstruction.Close)
//        // Checking for non-null context seems to be the best way to make sure parentFragmentManager will
//        // not throw an IllegalStateException when there is no parent fragment manager
//        val differentFragmentManagers = previousFragment?.context != null && previousFragment.parentFragmentManager != context.fragment.parentFragmentManager
//
//        context.fragment.parentFragmentManager.commitNow {
//            setCustomAnimations(animations.enter, animations.exit)
//            remove(context.fragment)
//
//            if (previousFragment != null && !differentFragmentManagers) {
//                when {
//                    previousFragment.isDetached -> attach(previousFragment)
//                    !previousFragment.isAdded -> add(context.contextReference.id, previousFragment)
//                }
//            }
//
//            if(previousFragment != null && !differentFragmentManagers && previousFragmentInContainer == previousFragment) {
//                addSharedElementsForClose(context, previousFragment)
//            }
//
//            if (previousFragmentInContainer != null && previousFragmentInContainer != previousFragment) {
//                if(previousFragmentInContainer.isDetached) attach(previousFragmentInContainer)
//                val contextIsPrimaryFragment = context.fragment.parentFragmentManager.primaryNavigationFragment == context.fragment
//                if(contextIsPrimaryFragment) {
//                    setPrimaryNavigationFragment(previousFragmentInContainer)
//                }
//            }
//
//            if(!differentFragmentManagers && context.fragment == context.fragment.parentFragmentManager.primaryNavigationFragment){
//                setPrimaryNavigationFragment(previousFragment)
//            }
//        }
//
//        if(previousFragment != null && differentFragmentManagers) {
//            if(previousFragment.parentFragmentManager.primaryNavigationFragment != previousFragment) {
//                previousFragment.parentFragmentManager.commitNow {
//                    setPrimaryNavigationFragment(previousFragment)
//                }
//            }
//        }
//    }
//
//    fun FragmentTransaction.addSharedElementsForClose(
//        context: NavigationContext<out Fragment>,
//        previousFragment: Fragment
//    ) {
//        val elements = context.getNavigationHandleViewModel().instruction.getSharedElements()
//        if(elements.isEmpty()) return
//        previousFragment.postponeEnterTransition()
//        previousFragment.sharedElementEnterTransition = AutoTransition()
//        previousFragment.sharedElementReturnTransition = AutoTransition()
//
//        elements
//            .forEach {
//                addSharedElement(
//                    context.contextReference.requireView()
//                        .findViewById<View>(it.opens)
//                        .also { v -> v.transitionName = it.transitionName },
//                    it.transitionName
//                )
//            }
//
//        runOnCommit {
//            elements
//                .forEach {
//                    previousFragment.requireView()
//                        .findViewById<View>(it.from)
//                        .transitionName = it.transitionName
//                }
//            previousFragment.startPostponedEnterTransition()
//        }
//    }
//
//

    private fun openFragmentAsActivity(
        fromContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Open
    ) {
        if(fromContext.contextReference is DialogFragment && instruction.navigationDirection == NavigationDirection.REPLACE) {
            // If we attempt to openFragmentAsActivity into a DialogFragment using the REPLACE direction,
            // the Activity hosting the DialogFragment will be closed/replaced
            // Instead, we close the fromContext's DialogFragment and call openFragmentAsActivity with the instruction changed to a forward direction
            openFragmentAsActivity(fromContext, instruction.internal.copy(navigationDirection = NavigationDirection.FORWARD))
            fromContext.contextReference.dismiss()
            return
        }

        fromContext.controller.open(
            fromContext,
            NavigationInstruction.Open.OpenInternal(
                navigationDirection = instruction.navigationDirection,
                navigationKey = SingleFragmentKey(instruction.internal.copy(
                    navigationDirection = NavigationDirection.FORWARD,
                    parentInstruction = null
                ))
            )
        )
    }


// TODO - simplify
//private fun NavigationContext<out Fragment>.getPreviousFragment(): Fragment? {
//    val previouslyActiveFragment = getNavigationHandleViewModel().instruction.internal.previouslyActiveId
//        ?.let { previouslyActiveId ->
//            fragment.parentFragmentManager.fragments.firstOrNull {
//                it.getNavigationHandle().id == previouslyActiveId && it.isVisible
//            }
//        }
//
//    val containerView = contextReference.id
//    val parentInstruction = getNavigationHandleViewModel().instruction.internal.parentInstruction
//    parentInstruction ?: return previouslyActiveFragment
//
//    val previousNavigator = controller.navigatorForKeyType(parentInstruction.navigationKey::class)
//    if (previousNavigator is ComposableNavigator) {
//        return fragment.parentFragmentManager.findFragmentByTag(getNavigationHandleViewModel().instruction.internal.previouslyActiveId)
//    }
//    if(previousNavigator !is FragmentNavigator) return previouslyActiveFragment
//    val previousHost = fragmentHostFor(parentInstruction)
//    val previousFragment = previousHost?.fragmentManager?.findFragmentByTag(parentInstruction.instructionId)
//
//    return when {
//        previousFragment != null -> previousFragment
//        previousHost?.containerId == containerView -> previousHost.fragmentManager.fragmentFactory
//            .instantiate(
//                previousNavigator.contextType.java.classLoader!!,
//                previousNavigator.contextType.java.name
//            )
//            .apply {
//                arguments = Bundle().addOpenInstruction(
//                    parentInstruction.copy(
//                        children = emptyList()
//                    )
//                )
//            }
//        else -> previousHost?.fragmentManager?.findFragmentById(previousHost.containerId)
//    } ?: previouslyActiveFragment
//}
