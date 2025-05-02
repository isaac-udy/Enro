package dev.enro.core.window

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityOptionsCompat
import dev.enro.animation.NavigationAnimationForView
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.activity
import dev.enro.core.addOpenInstruction
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application
import dev.enro.core.controller.get
import dev.enro.core.controller.isInAndroidContext
import dev.enro.core.controller.usecase.GetNavigationAnimations
import dev.enro.core.controller.usecase.GetNavigationBinding
import dev.enro.core.hosts.ActivityHost
import dev.enro.core.internal.EnroLog
import dev.enro.core.navigationContext
import dev.enro.core.plugins.EnroPlugin
import dev.enro.core.result.EnroResult

public actual class NavigationWindowManager actual constructor(
    private val controller: NavigationController,
) : EnroPlugin() {
    private val activityHost = ActivityHost()
    private val getNavigationAnimations = controller.dependencyScope.get<GetNavigationAnimations>()

    private var activities = listOf<Activity>()

    private var activityLifecycleListener: Application.ActivityLifecycleCallbacks? = null

    override fun onAttached(navigationController: NavigationController) {
        if (navigationController.isInAndroidContext) {
            activityLifecycleListener = ActivityLifecycleListener()
            val application = navigationController.application
            application.registerActivityLifecycleCallbacks(activityLifecycleListener)
        }
    }

    override fun onDetached(navigationController: NavigationController) {
        if (navigationController.isInAndroidContext) {
            val application = navigationController.application
            if (activityLifecycleListener != null) {
                application.unregisterActivityLifecycleCallbacks(activityLifecycleListener)
            }
        }
    }

    public actual fun open(instruction: AnyOpenInstruction) {
        val isOpen = activities
            .filterIsInstance<ComponentActivity>()
            .any { it.navigationContext.instruction.instructionId == instruction.instructionId }
        if (isOpen) return

        // TODO need to handle non-ComponentActivity cases, and null cases
        val activity = activities.lastOrNull()
        if (activity != null) {
            val exitingInstruction = (activity as? ComponentActivity)?.navigationContext?.instruction
            val instructionToOpenHosted = activityHost.wrap(controller, instruction)

            val binding = requireNotNull(
                controller.dependencyScope.get<GetNavigationBinding>()
                    .invoke(instructionToOpenHosted)
            ) { "Could not open ${instructionToOpenHosted.navigationKey::class.simpleName}: No NavigationBinding was found" }

            val intent = Intent(activity, binding.destinationType.java)
                .addOpenInstruction(instructionToOpenHosted)

            val animations = getNavigationAnimations.opening(
                type = NavigationAnimationForView::class,
                exiting = exitingInstruction,
                entering = instructionToOpenHosted,
            )
            val options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                animations.enterAsResource(activity),
                animations.exitAsResource(activity),
            )
            activity.startActivity(intent, options.toBundle())
        } else {
            EnroLog.warn("Could not open $instruction; there was no active activity")
        }
    }

    public actual fun close(context: NavigationContext<*>, andOpen: AnyOpenInstruction?) {
        val toClose = activities
            .firstOrNull { it == context.activity }
            ?: throw IllegalStateException("Could not find activity to close for $context")

        if (andOpen != null) {
            open(andOpen)
        }
        else {
            val animations = getNavigationAnimations.closing(
                type = NavigationAnimationForView::class,
                exiting = context.instruction,
                entering = andOpen,
            )

            @Suppress("DEPRECATION") // TODO stop using deprecated method overridePendingTransition
            toClose.overridePendingTransition(
                animations.enterAsResource(toClose),
                animations.exitAsResource(toClose),
            )
        }
        toClose.finish()
    }

    private inner class ActivityLifecycleListener : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities = activities.filter { it != activity } + activity
        }

        override fun onActivityStarted(activity: Activity) {
            activities = activities.filter { it != activity } + activity
            checkResults(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            activities = activities.filter { it != activity } + activity
            checkResults(activity)
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            activities = activities.filter { it != activity }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}

        private fun checkResults(activity: Activity) {
            if (activity !is ComponentActivity) return
            val context = activity.navigationContext
            val result = EnroResult.from(context.controller)
            if (result.hasPendingResultFrom(context.instruction)) {
                activity.finish()
            }
        }
    }
}

/*
override fun onBackstackUpdated(transition: NavigationBackstackTransition): Boolean {
        // When the backstack is updated, we need to check if there are pending results and close
        // immediately to ensure forwarding results work correctly
        val result = EnroResult.from(context.controller)
        if (result.hasPendingResultFrom(context.instruction)) {
            context.activity.finish()
            return true
        }

        val activity = weakActivity.get() ?: return true
        val activeInstruction = activity.navigationContext.instruction
        if (transition.activeBackstack.singleOrNull()?.instructionId == activeInstruction.instructionId) return true
        val childContext = requireNotNull(childContext)
        setBackstack(backstackOf(activeInstruction))

        val activeInstructionIsPresent = transition.activeBackstack.any { it.instructionId == activeInstruction.instructionId }
        if (!activeInstructionIsPresent) {
            ActivityCompat.finishAfterTransition(childContext.activity)
//            val animations = getNavigationAnimations.closing(
//                exiting = activeInstruction,
//                entering = transition.activeBackstack.active,
//            )
            childContext.activity.overridePendingTransition(
                0,0
//                animations.entering.asResource(childContext.activity.theme).id,
//                animations.exiting.asResource(childContext.activity.theme).id
            )
        }

        val instructionToOpen = transition.activeBackstack
            .filter { it.instructionId != activeInstruction?.instructionId }
            .also {
                require(it.size <= 2) { transition.activeBackstack.joinToString { it.navigationKey.toString() } }
            }
            .firstOrNull() ?: return true

        val instructionToOpenHosted = childContext.controller.dependencyScope.get<HostInstructionAs>().invoke<Activity>(
            childContext,
            instructionToOpen
        )
        val binding = requireNotNull(
            childContext.controller.dependencyScope.get<GetNavigationBinding>()
                .invoke(instructionToOpenHosted)
        ) { "Could not open ${instructionToOpenHosted.navigationKey::class.simpleName}: No NavigationBinding was found" }

        val intent = Intent(childContext.activity, binding.destinationType.java)
            .addOpenInstruction(instructionToOpenHosted)

        val animations = getNavigationAnimations.opening(
            exiting = activeInstruction,
            entering = instructionToOpenHosted
        )

        val options = ActivityOptionsCompat.makeCustomAnimation(
            activity, 0,0
//            animations.entering.asResource(childContext.activity.theme).id,
//            animations.exiting.asResource(childContext.activity.theme).id
        )
        activity.startActivity(intent, options.toBundle())

        return true
    }
 */