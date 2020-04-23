package nav.enro.core.internal.executors

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import nav.enro.core.*
import nav.enro.core.internal.closeEnterAnimation
import nav.enro.core.internal.closeExitAnimation
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.context.activityFromContext
import nav.enro.core.internal.openEnterAnimation
import nav.enro.core.internal.openExitAnimation

internal class ActivityNavigationExecutor : NavigationExecutor() {
    override fun open(
        navigator: Navigator<*>,
        fromContext: NavigationContext<out Any, *>,
        instruction: NavigationInstruction.Open<*>
    ) {
        navigator as ActivityNavigator
        val override = fromContext.controller.pendingOverrideFor(
            fromContext.contextReference,
            navigator.contextType
        )

        val intent = createIntentFor(navigator, fromContext, instruction)

        if (override != null) {
            override.launchActivity(instruction, intent)
        } else {
            val activity = fromContext.activityFromContext
            if (instruction.navigationDirection == NavigationDirection.REPLACE || instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
                activity.finish()
            }
            activity.startActivity(intent)
            activity.overridePendingTransition(
                activity.openEnterAnimation,
                activity.openExitAnimation
            )
        }
    }

    override fun close(context: NavigationContext<out Any, *>) {
        context as ActivityContext<out FragmentActivity, *>

        val fromContext = context.parentInstruction
            ?.let { context.controller.navigatorForKeyType(it.navigationKey::class) }
            ?.contextType

        val override = context.controller.overrideFor(
            fromContext,
            context.contextType
        )

        if (override != null) {
            override.closeActivity(context.activity)
        } else {
            context.activity.finish()
            context.activity.overridePendingTransition(
                context.activity.closeEnterAnimation,
                context.activity.closeExitAnimation
            )
        }
    }

    private fun createIntentFor(
        navigator: ActivityNavigator<*>,
        fromContext: NavigationContext<out Any, *>,
        instruction: NavigationInstruction.Open<*>
    ): Intent {
        val parentInstruction = when (instruction.navigationDirection) {
            NavigationDirection.REPLACE_ROOT -> null
            NavigationDirection.REPLACE -> fromContext.instruction?.parentInstruction
            NavigationDirection.FORWARD -> fromContext.instruction
        }
        val parentActivityInstruction =
            getParentActivityInstruction(fromContext.controller, parentInstruction)
        val nextInstruction = instruction.copy(
            parentInstruction = parentActivityInstruction?.copy(parentInstruction = null)
        )

        val intent = Intent(fromContext.activityFromContext, navigator.contextType.java)
            .addOpenInstruction(nextInstruction)

        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        return intent
    }

    private fun getParentActivityInstruction(
        navigationController: NavigationController,
        instruction: NavigationInstruction.Open<*>?
    ): NavigationInstruction.Open<*>? {
        if (instruction == null) return null
        val keyType = instruction.navigationKey::class
        val navigator = navigationController.navigatorForKeyType(keyType)
        if (navigator is ActivityNavigator) return instruction
        return getParentActivityInstruction(navigationController, instruction.parentInstruction)
    }
}