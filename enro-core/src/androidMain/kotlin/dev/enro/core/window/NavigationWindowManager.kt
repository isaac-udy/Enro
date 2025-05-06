package dev.enro.core.window

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityOptionsCompat
import dev.enro.animation.NavigationAnimationForView
import dev.enro.animation.NavigationAnimationOverride
import dev.enro.animation.findDefaults
import dev.enro.animation.findOverrideForClosing
import dev.enro.animation.findOverrideForOpening
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.addOpenInstruction
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application
import dev.enro.core.controller.get
import dev.enro.core.controller.isInAndroidContext
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
    private val animationOverride = controller.dependencyScope.get<NavigationAnimationOverride>()

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

            val default = animationOverride.findDefaults(
                NavigationAnimationForView::class
            )
            val animations = animationOverride.findOverrideForOpening(
                type = NavigationAnimationForView::class,
                exiting = exitingInstruction,
                entering = instructionToOpenHosted,
            ) ?: when(instructionToOpenHosted.navigationDirection) {
                NavigationDirection.Present -> default.present
                NavigationDirection.Push -> default.push
            }
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
            .firstOrNull { it == context.contextReference }
            ?: throw IllegalStateException("Could not find activity to close for $context")

        if (andOpen != null) {
            open(andOpen)
        }
        else {
            val defaults = animationOverride.findDefaults(
                NavigationAnimationForView::class
            )
            val animations = animationOverride.findOverrideForClosing(
                type = NavigationAnimationForView::class,
                exiting = context.instruction,
                entering = andOpen,
            ) ?: when (context.instruction.navigationDirection) {
                NavigationDirection.Present -> defaults.presentReturn
                NavigationDirection.Push -> defaults.pushReturn
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                toClose.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_CLOSE,
                    animations.enterAsResource(toClose),
                    animations.exitAsResource(toClose),
                    Color.TRANSPARENT,
                )
            } else {
                @Suppress("DEPRECATION")
                toClose.overridePendingTransition(
                    animations.enterAsResource(toClose),
                    animations.exitAsResource(toClose),
                )
            }
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
