package nav.enro.core.executors

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationDirection
import nav.enro.core.NavigationKey
import nav.enro.core.addOpenInstruction
import nav.enro.core.context.NavigationContext
import nav.enro.core.context.activity
import nav.enro.core.context.parentActivity
import nav.enro.core.navigator.*

object DefaultActivityExecutor : NavigationExecutor<Any, FragmentActivity, NavigationKey>(
    fromType = Any::class,
    opensType = FragmentActivity::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<Any, FragmentActivity, NavigationKey>) {
        val fromContext = args.fromContext
        val navigator = args.navigator
        val instruction = args.instruction

        navigator as ActivityNavigator

        val intent =  Intent(fromContext.parentActivity, navigator.contextType.java)
            .addOpenInstruction(instruction)

        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val activity = fromContext.parentActivity
        if (instruction.navigationDirection == NavigationDirection.REPLACE || instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            activity.finish()
        }
        val animations = navigator.animations
            .toResource(fromContext.parentActivity)
            .animationsForOpen(instruction.navigationDirection)

        activity.startActivity(intent)
        activity.overridePendingTransition(animations.first, animations.second)
    }

    override fun close(context: NavigationContext<out FragmentActivity, out NavigationKey>) {
        context.activity.finish()

        val animations = context.navigator.animations
            .toResource(context.parentActivity)
            .animationsForClose()

        context.activity.overridePendingTransition(animations.first, animations.second)
    }
}