package dev.enro.core.result.flows

import androidx.savedstate.read
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.usecase.AddPendingResult
import dev.enro.core.readOpenInstruction

internal object NavigationFlowInterceptor : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? {
        if (instruction !is NavigationInstruction.Close.WithResult) return instruction
        val openInstruction = context.arguments.readOpenInstruction() ?: return instruction

        val navigationKey = openInstruction.navigationKey
        if (navigationKey !is NavigationKey.WithResult<*>) return instruction

        val isFlowResult = openInstruction.extras.read {
            if (!contains(NavigationFlow.IS_PUSHED_IN_FLOW)) return@read false
            getBoolean(NavigationFlow.IS_PUSHED_IN_FLOW)
        }
        if (!isFlowResult) return instruction

        val addPendingResult = context.controller.dependencyScope.get<AddPendingResult>()
        addPendingResult(
            context,
            instruction,
        )
        return null
    }
}