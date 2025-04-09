package dev.enro.core.activity

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.activity
import dev.enro.core.addOpenInstruction
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.acceptAll
import dev.enro.core.container.backstackOf
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.GetNavigationBinding
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.navigationContext
import dev.enro.core.result.EnroResult
import java.lang.ref.WeakReference

internal class ActivityNavigationContainer internal constructor(
    applicationContext: NavigationContext<out Application>,
) : NavigationContainer(
    key = NavigationContainerKey.FromName("ActivityNavigationContainer"),
    context = applicationContext,
    contextType = Activity::class,
    emptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor = { },
    animations = { },
    instructionFilter = acceptAll(),
) {
    private var weakActivity = WeakReference<ComponentActivity>(null)

    override val isVisible: Boolean
        get() = context.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    init {
        applicationContext.contextReference.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                if (activity !is ComponentActivity) return
                weakActivity = WeakReference(activity)
                setBackstack(backstackOf(activity.navigationContext.instruction))
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                if (activity !is ComponentActivity) return
                if (weakActivity.get() == activity) {
                    weakActivity = WeakReference(null)
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    override fun getChildContext(contextFilter: ContextFilter): NavigationContext<*>? {
        return weakActivity.get()?.navigationContext
    }

    override fun onBackstackUpdated(transition: NavigationBackstackTransition): Boolean {
        // When the backstack is updated, we need to check if there are pending results and close
        // immediately to ensure forwarding results work correctly
        val result = EnroResult.from(context.controller)
        if (result.hasPendingResultFrom(context.instruction)) {
            context.activity.finish()
            return true
        }
        val activeInstruction = weakActivity.get()?.navigationContext?.instruction ?: return true
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

        val activity = childContext.activity

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
}