package dev.enro.core.activity

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.backstackOf
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.GetNavigationBinding
import dev.enro.core.controller.usecase.HostInstructionAs

internal class ActivityNavigationContainer internal constructor(
    activityContext: NavigationContext<out ComponentActivity>,
) : NavigationContainer(
    key = NavigationContainerKey.FromName("ActivityNavigationContainer"),
    parentContext = activityContext,
    contextType = Activity::class.java,
    emptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor = { },
    acceptsNavigationKey = { true },
    acceptsDirection = { true },
) {
    override val activeContext: NavigationContext<*>
        get() = parentContext

    override val isVisible: Boolean
        get() = parentContext.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    override val currentAnimations: NavigationAnimation
        get() = animationsFor(parentContext, parentContext.getNavigationHandle().instruction)

    private val rootInstruction: AnyOpenInstruction
        get() = activeContext.getNavigationHandle().instruction

    init {
        setBackstack(backstackOf(rootInstruction))
    }

    override fun onBackstackUpdated(transition: NavigationBackstackTransition): Boolean {
        if (transition.activeBackstack.singleOrNull()?.instructionId == rootInstruction.instructionId) return true
        setBackstack(backstackOf(rootInstruction))

        val activeInstructionIsPresent = transition.activeBackstack.any { it.instructionId == rootInstruction.instructionId }
        if (!activeInstructionIsPresent) {
            ActivityCompat.finishAfterTransition(activeContext.activity)
            val animations = animationsFor(activeContext, NavigationInstruction.Close).asResource(activeContext.activity.theme)
            activeContext.activity.overridePendingTransition(animations.enter, animations.exit)
        }

        val instructionToOpen = transition.activeBackstack
            .filter { it.instructionId != rootInstruction.instructionId }
            .also {
                require(it.size <= 2) { transition.activeBackstack.joinToString { it.navigationKey.toString() } }
            }
            .firstOrNull() ?: return true

        val instructionToOpenHosted = activeContext.controller.dependencyScope.get<HostInstructionAs>().invoke<Activity>(
            activeContext,
            instructionToOpen
        )
        val binding = requireNotNull(
            activeContext.controller.dependencyScope.get<GetNavigationBinding>()
                .invoke(instructionToOpenHosted)
        )

        val intent = Intent(activeContext.activity, binding.destinationType.java)
            .addOpenInstruction(instructionToOpenHosted)

        if (instructionToOpen.navigationDirection == NavigationDirection.ReplaceRoot) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val activity = activeContext.activity
        val animations = animationsFor(activeContext, instructionToOpenHosted).asResource(activity.theme)

        val options = ActivityOptionsCompat.makeCustomAnimation(activity, animations.enter, animations.exit)
        activity.startActivity(intent, options.toBundle())

        return true
    }
}