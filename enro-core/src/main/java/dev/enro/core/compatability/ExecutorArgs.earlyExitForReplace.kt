package dev.enro.core.compatability

import android.app.Activity
import dev.enro.core.*
import dev.enro.core.container.asDirection
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.controller.usecase.HostInstructionAs

internal fun ExecutorArgs<*, *, *>.earlyExitForReplace(): Boolean {
    val isReplace = instruction.navigationDirection is NavigationDirection.Replace

    val isReplaceActivity = fromContext is ActivityContext && isReplace
    if (!isReplaceActivity) return false

    openInstructionAsActivity(fromContext, NavigationDirection.Present, instruction)
    fromContext.activity.finish()
    return true
}

private fun openInstructionAsActivity(
    fromContext: NavigationContext<out Any>,
    navigationDirection: NavigationDirection,
    instruction: AnyOpenInstruction
) {
    val open = fromContext.controller.dependencyScope.get<ExecuteOpenInstruction>()
    val hostInstructionAs = fromContext.controller.dependencyScope.get<HostInstructionAs>()

    open.invoke(
        fromContext,
        hostInstructionAs<Activity>(
            fromContext,
            instruction.asDirection(navigationDirection)
        ),
    )
}