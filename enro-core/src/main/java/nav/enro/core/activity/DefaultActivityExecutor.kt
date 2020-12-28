package nav.enro.core.activity

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import nav.enro.core.*
import nav.enro.core.NavigationContext
import nav.enro.core.activity

object DefaultActivityExecutor : NavigationExecutor<Any, FragmentActivity, NavigationKey>(
    fromType = Any::class,
    opensType = FragmentActivity::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out FragmentActivity, out NavigationKey>) {
        val fromContext = args.fromContext
        val navigator = args.navigator
        val instruction = args.instruction

        navigator as ActivityNavigator

        val intent = createIntent(args)

        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val activity = fromContext.activity
        if (instruction.navigationDirection == NavigationDirection.REPLACE || instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            activity.finish()
        }
        val animations = animationsFor(fromContext, instruction)

        activity.startActivity(intent)
        if (instruction.children.isEmpty()) {
            activity.overridePendingTransition(animations.enter, animations.exit)
        } else {
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun close(context: NavigationContext<out FragmentActivity>) {
        context.activity.supportFinishAfterTransition()
        context.navigator ?: return

        val animations = animationsFor(context, NavigationInstruction.Close)
        context.activity.overridePendingTransition(animations.enter, animations.exit)
    }

    fun createIntent(args: ExecutorArgs<out Any, out FragmentActivity, out NavigationKey>) =
        Intent(args.fromContext.activity, args.navigator.contextType.java)
            .addOpenInstruction(args.instruction)
}