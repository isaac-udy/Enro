package dev.enro.core.result.flows

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.usecase.AddPendingResult

internal object NavigationFlowInterceptor : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? {
        if (instruction !is NavigationInstruction.Close.WithResult) return instruction
        val openInstruction = context.instruction

        val navigationKey = openInstruction.navigationKey
        if (navigationKey !is NavigationKey.WithResult<*>) return instruction

        val isFlowResult = openInstruction.extras.get(NavigationFlow.IS_PUSHED_IN_FLOW) ?: false
        if (!isFlowResult) return instruction

        val addPendingResult = context.controller.dependencyScope.get<AddPendingResult>()
        addPendingResult(
            context,
            instruction,
        )
        return null
    }
}