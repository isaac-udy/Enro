package dev.enro.core.activity

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import dev.enro.core.*

object DefaultActivityExecutor : NavigationExecutor<Any, ComponentActivity, NavigationKey>(
    fromType = Any::class,
    opensType = ComponentActivity::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out ComponentActivity, out NavigationKey>) {
        val fromContext = args.fromContext
        val navigator = args.navigator
        val instruction = args.instruction

        navigator as ActivityNavigator

        val intent = createIntent(args)

        if (instruction.navigationDirection == NavigationDirection.ReplaceRoot) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val activity = fromContext.activity
        if (instruction.navigationDirection == NavigationDirection.Replace || instruction.navigationDirection == NavigationDirection.ReplaceRoot) {
            activity.finish()
        }
        val animations = animationsFor(fromContext, instruction).asResource(activity.theme)

        activity.startActivity(intent)
        if (instruction.children.isEmpty()) {
            activity.overridePendingTransition(animations.enter, animations.exit)
        } else {
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun close(context: NavigationContext<out ComponentActivity>) {
        ActivityCompat.finishAfterTransition(context.activity)
        context.navigator ?: return

        val animations = animationsFor(context, NavigationInstruction.Close).asResource(context.activity.theme)
        context.activity.overridePendingTransition(animations.enter, animations.exit)
    }

    fun createIntent(args: ExecutorArgs<out Any, out ComponentActivity, out NavigationKey>) =
        Intent(args.fromContext.activity, args.navigator.contextType.java)
            .addOpenInstruction(args.instruction)
}