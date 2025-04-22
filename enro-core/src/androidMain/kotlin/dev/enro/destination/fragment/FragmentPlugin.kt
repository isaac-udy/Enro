package dev.enro.destination.fragment

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.activity
import dev.enro.core.close
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainerProperty
import dev.enro.core.container.accept
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.findContainerFor
import dev.enro.core.containerManager
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application
import dev.enro.core.controller.createNavigationModule
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.isInAndroidContext
import dev.enro.core.controller.usecase.AddPendingResult
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.fragment.container.FragmentNavigationContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationContext
import dev.enro.core.parentContainer
import dev.enro.core.plugins.EnroPlugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object FragmentPlugin : EnroPlugin() {
    private var callbacks: FragmentLifecycleCallbacksForEnro? = null

    override fun onAttached(navigationController: NavigationController) {
        if (!navigationController.isInAndroidContext) return

        navigationController.addModule(createNavigationModule {
            interceptor(object : NavigationInstructionInterceptor {
                override fun intercept(
                    instruction: AnyOpenInstruction,
                    context: NavigationContext<*>,
                    binding: NavigationBinding<out NavigationKey, out Any>
                ): AnyOpenInstruction? {
                    if (context.contextReference is Fragment && !context.contextReference.isAdded) {
                        return null
                    }

                    // This is legacy logic that will allow an instruction that would otherwise be pushed into an ActivityNavigationContainer
                    // to be switched into a Present Instruction and opened into the default fragment container, which
                    // will result in the destination being opened as a full screen dialog
                    val container = findContainerFor(context, instruction)
                    val defaultFragmentContainer = context.activity.containerManager.getContainer(NavigationContainerKey.FromId(android.R.id.content))
                    if (instruction.navigationDirection == NavigationDirection.Push && container == null && defaultFragmentContainer != null) {
                        EnroException.MissingContainerForPushInstruction.logForStrictMode(
                            navigationController = context.controller,
                            navigationKey = instruction.navigationKey,
                        )
                        defaultFragmentContainer.context.navigationHandle.executeInstruction(instruction.asPresentInstruction())
                        return null
                    }
                    return instruction
                }

                override fun intercept(
                    instruction: NavigationInstruction.Close,
                    context: NavigationContext<*>
                ): NavigationInstruction? {
                    if (earlyExitForNoContainer(context)) {
                        // TODO: add test for unbound pending results in fragments
                        navigationController.dependencyScope
                            .get<AddPendingResult>()
                            .invoke(context, instruction)
                        return null
                    }
                    return super.intercept(instruction, context)
                }
            })
        })

        callbacks = FragmentLifecycleCallbacksForEnro(
            navigationController.dependencyScope.get(),
            navigationController.dependencyScope.get(),
        ).also { callbacks ->
            navigationController.application.registerActivityLifecycleCallbacks(callbacks)
        }
    }

    override fun onDetached(navigationController: NavigationController) {
        if (!navigationController.isInAndroidContext) return

        callbacks?.let { callbacks ->
            navigationController.application.unregisterActivityLifecycleCallbacks(callbacks)
        }
        callbacks = null
    }
}

private class FragmentLifecycleCallbacksForEnro(
    private val onNavigationContextCreated: OnNavigationContextCreated,
    private val onNavigationContextSaved: OnNavigationContextSaved,
) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is FragmentActivity)  return
        activity.supportFragmentManager
            .registerFragmentLifecycleCallbacks(fragmentCallbacks, true)

        NavigationContainerProperty(
            lifecycleOwner = activity,
            navigationContainerProducer = {
                FragmentNavigationContainer(
                    containerId = android.R.id.content,
                    parentContext = activity.navigationContext,
                    filter = accept { anyPresented() },
                    emptyBehavior = EmptyBehavior.AllowEmpty,
                    interceptor = {},
                    animations = {},
                    initialBackstack = emptyBackstack(),
                )
            },
            onContainerAttached = {
                if (activity.containerManager.activeContainer != it) return@NavigationContainerProperty
                if (savedInstanceState != null) return@NavigationContainerProperty
                activity.containerManager.setActiveContainer(null)
            }
        )
    }

    private val fragmentCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentPreCreated(
            fm: FragmentManager,
            fragment: Fragment,
            savedInstanceState: Bundle?
        ) {
            // TODO throw exception if fragment is opened into an Enro registered NavigationContainer without
            // being opened through Enro
            onNavigationContextCreated(FragmentContext(fragment), savedInstanceState)
        }

        override fun onFragmentSaveInstanceState(
            fm: FragmentManager,
            fragment: Fragment,
            outState: Bundle
        ) {
            onNavigationContextSaved(fragment.navigationContext, outState)
        }
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}

internal fun earlyExitForNoContainer(context: NavigationContext<*>) : Boolean {
    if (context.contextReference !is Fragment) return false

    val container = context.parentContainer()
    if (container != null) return false

    /*
     * There are some cases where a Fragment's FragmentManager can be removed from the Fragment.
     * There is (as far as I am aware) no easy way to check for the FragmentManager being removed from the
     * Fragment, other than attempting to catch the exception that is thrown in the case of a missing
     * parentFragmentManager.
     *
     * If a Fragment's parentFragmentManager has been destroyed or removed, there's very little we can
     * do to resolve the problem, and the most likely case is if
     *
     * The most common case where this can occur is if a DialogFragment is closed in response
     * to a nested Fragment closing with a result - this causes the DialogFragment to close,
     * and then for the nested Fragment to attempt to close immediately afterwards, which fails because
     * the nested Fragment is no longer attached to any fragment manager (and won't be again).
     *
     * see ResultTests.whenResultFlowIsLaunchedInDialogFragment_andCompletesThroughTwoNestedFragments_thenResultIsDelivered
     */
    runCatching {
        context.contextReference.parentFragmentManager
    }
        .onSuccess { fragmentManager ->
            runCatching { fragmentManager.executePendingTransactions() }
                .onFailure {
                    // if we failed to execute pending transactions, we're going to
                    // re-attempt to close this context (by executing "close" on it's NavigationHandle)
                    // but we're going to delay for 1 millisecond first, which will allow the
                    // main thread to finish executing the transaction before attempting the close
                    val navigationHandle = context.contextReference.getNavigationHandle()
                    navigationHandle.lifecycleScope.launch {
                        delay(1)
                        navigationHandle.close()
                    }
                }
                .onSuccess {
                    fragmentManager.commitNow {
                        setReorderingAllowed(true)
                        remove(context.contextReference)
                    }
                }
        }
    return true
}