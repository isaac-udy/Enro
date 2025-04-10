package dev.enro.destination.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.close
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application
import dev.enro.core.controller.createNavigationModule
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.isInAndroidContext
import dev.enro.core.controller.usecase.AddPendingResult
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.navigationContext
import dev.enro.core.parentContainer
import dev.enro.core.plugins.EnroPlugin

internal object ActivityPlugin : EnroPlugin() {

    private var callbacks: ActivityLifecycleCallbacksForEnro? = null

    override fun onAttached(navigationController: NavigationController) {
        if (!navigationController.isInAndroidContext) return
        val applicationContext = ApplicationContext(navigationController.application, navigationController)
        callbacks = ActivityLifecycleCallbacksForEnro(
            applicationContext,
            navigationController.dependencyScope.get(),
            navigationController.dependencyScope.get(),
        ).also { callbacks ->
            navigationController.application.registerActivityLifecycleCallbacks(callbacks)
        }

        navigationController.addModule(
            createNavigationModule {
                interceptor(object : NavigationInstructionInterceptor {
                    override fun intercept(
                        instruction: NavigationInstruction.Close,
                        context: NavigationContext<*>
                    ): NavigationInstruction? {
                        if (instruction !is NavigationInstruction.Close.AndThenOpen) return instruction
                        if (context.contextReference !is ComponentActivity) return instruction
                        val container = requireNotNull(context.parentContainer()) {
                            // TODO better error message
                        }
                        container.setBackstack(
                            container.backstack
                                .close(id = context.instruction.instructionId)
                                .plus(instruction.instruction)
                                .toBackstack()
                        )

                        navigationController.dependencyScope.get<AddPendingResult>()
                            .invoke(context, instruction)
                        return null
                    }
                })
            }
        )
    }

    override fun onDetached(navigationController: NavigationController) {
        if (!navigationController.isInAndroidContext) return

        callbacks?.let { callbacks ->
            navigationController.application.unregisterActivityLifecycleCallbacks(callbacks)
        }
        callbacks = null
    }
}

private class ActivityLifecycleCallbacksForEnro(
    private val applicationContext: NavigationContext<out Application>,
    private val onNavigationContextCreated: OnNavigationContextCreated,
    private val onNavigationContextSaved: OnNavigationContextSaved,
) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        if (activity !is ComponentActivity) return
        val navigationContext = ActivityContext(applicationContext, activity)
        activity.theme.applyStyle(android.R.style.Animation_Activity, false)
        onNavigationContextCreated(navigationContext, savedInstanceState)
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
        if (activity !is ComponentActivity) return
        onNavigationContextSaved(activity.navigationContext, outState)
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}