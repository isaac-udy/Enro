package nav.enro.core.internal.executors

import android.content.Intent
import nav.enro.core.internal.closeEnterAnimation
import nav.enro.core.internal.closeExitAnimation
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.openEnterAnimation
import nav.enro.core.internal.openExitAnimation
import nav.enro.core.ActivityNavigator
import nav.enro.core.NavigationDirection
import nav.enro.core.NavigationInstruction
import nav.enro.core.Navigator

internal class ActivityNavigationExecutor : NavigationExecutor {
    override fun open(
        navigator: Navigator<*>,
        fromContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open
    ) {
        navigator as ActivityNavigator

        val activity = when (fromContext) {
            is FragmentContext -> fromContext.fragment.requireActivity()
            is ActivityContext -> fromContext.activity
        }

        val startActivity: (Intent) -> Unit = when (fromContext) {
            is FragmentContext -> fromContext.fragment::startActivity
            is ActivityContext -> fromContext.activity::startActivity
        }

        val intent = Intent(activity, navigator.contextType.java).apply {
            putExtra(NavigationContext.ARG_NAVIGATION_KEY, instruction.navigationKey)
            putParcelableArrayListExtra(NavigationContext.ARG_CHILDREN, ArrayList(instruction.children))
        }

        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        if (instruction.navigationDirection == NavigationDirection.REPLACE) {
            activity.finish()
        }
        startActivity(intent)
        activity.overridePendingTransition(
            activity.openEnterAnimation,
            activity.openExitAnimation
        )
    }

    override fun close(context: NavigationContext<*>) {
        context as ActivityContext

        context.activity.finish()
        context.activity.overridePendingTransition(
            context.activity.closeEnterAnimation,
            context.activity.closeExitAnimation
        )
    }
}