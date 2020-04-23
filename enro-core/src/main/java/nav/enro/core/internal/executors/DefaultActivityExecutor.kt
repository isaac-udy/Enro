package nav.enro.core.internal.executors

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import nav.enro.core.*
import nav.enro.core.internal.closeEnterAnimation
import nav.enro.core.internal.closeExitAnimation
import nav.enro.core.internal.context.*
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.internal.context.activity
import nav.enro.core.internal.openEnterAnimation
import nav.enro.core.internal.openExitAnimation

object DefaultActivityExecutor : NavigationExecutor<Any, FragmentActivity, NavigationKey>(
    fromType = Any::class,
    opensType = FragmentActivity::class,
    keyType = NavigationKey::class
) {
    override fun open(
        fromContext: NavigationContext<out Any, *>,
        navigator: Navigator<out FragmentActivity, out NavigationKey>,
        instruction: NavigationInstruction.Open<out NavigationKey>
    ) {
        navigator as ActivityNavigator

        val intent =  Intent(fromContext.requireActivity(), navigator.contextType.java)
            .addOpenInstruction(instruction)

        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val activity = fromContext.requireActivity()
        if (instruction.navigationDirection == NavigationDirection.REPLACE || instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            activity.finish()
        }
        activity.startActivity(intent)
        activity.overridePendingTransition(
            activity.openEnterAnimation,
            activity.openExitAnimation
        )
    }

    override fun close(context: NavigationContext<out FragmentActivity, out NavigationKey>) {
        context.activity.finish()
        context.activity.overridePendingTransition(
            context.activity.closeEnterAnimation,
            context.activity.closeExitAnimation
        )
    }
}