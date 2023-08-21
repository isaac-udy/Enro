package dev.enro.destination.activity

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import dev.enro.android.activity
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.addOpenInstruction
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.backstackOf
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.GetNavigationBinding
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.getNavigationHandle

internal class ActivityNavigationContainer internal constructor(
    activityContext: NavigationContext<out ComponentActivity>,
) : NavigationContainer(
    key = NavigationContainerKey.FromName("ActivityNavigationContainer"),
    context = activityContext,
    contextType = Activity::class.java,
    emptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor = { },
    animations = { },
    acceptsNavigationKey = { true },
) {
    override val childContext: NavigationContext<*>
        get() = context

    override val isVisible: Boolean
        get() = context.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    private val rootInstruction: AnyOpenInstruction
        get() = childContext.getNavigationHandle().instruction

    init {
        setBackstack(backstackOf(rootInstruction))
    }

    override fun onBackstackUpdated(transition: NavigationBackstackTransition): Boolean {
        if (transition.activeBackstack.singleOrNull()?.instructionId == rootInstruction.instructionId) return true
        setBackstack(backstackOf(rootInstruction))

        val activeInstructionIsPresent = transition.activeBackstack.any { it.instructionId == rootInstruction.instructionId }
        if (!activeInstructionIsPresent) {
            ActivityCompat.finishAfterTransition(childContext.activity)
            val animations = getNavigationAnimations.closing(
                exiting = rootInstruction,
                entering = transition.activeBackstack.active,
            )
            childContext.activity.overridePendingTransition(
                animations.entering.asResource(childContext.activity.theme).id,
                animations.exiting.asResource(childContext.activity.theme).id
            )
        }

        val instructionToOpen = transition.activeBackstack
            .filter { it.instructionId != rootInstruction.instructionId }
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
        ) { "Could not open ${instructionToOpenHosted.navigationKey::class.java.simpleName}: No NavigationBinding was found" }

        val intent = Intent(childContext.activity, binding.destinationType.java)
            .addOpenInstruction(instructionToOpenHosted)

        if (instructionToOpen.navigationDirection == NavigationDirection.ReplaceRoot) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val activity = childContext.activity

        val animations = getNavigationAnimations.opening(
            exiting = rootInstruction,
            entering = instructionToOpenHosted
        )

        val options = ActivityOptionsCompat.makeCustomAnimation(
            activity,
            animations.entering.asResource(childContext.activity.theme).id,
            animations.exiting.asResource(childContext.activity.theme).id
        )
        activity.startActivity(intent, options.toBundle())

        return true
    }
}