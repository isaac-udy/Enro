package dev.enro.core.result

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.activity
import dev.enro.core.activity.ActivityNavigationContainer
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.usecase.AddPendingResult
import dev.enro.core.navigationContext
import dev.enro.core.readOpenInstruction
import dev.enro.core.result.flows.FlowStep
import dev.enro.core.rootContext

internal object ForwardingResultInterceptor  : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? {
        if (instruction !is NavigationInstruction.Close.WithResult) return instruction
        val openInstruction = context.arguments.readOpenInstruction() ?: return instruction

        val navigationKey = openInstruction.navigationKey
        if (navigationKey !is NavigationKey.WithResult<*>) return instruction

        val forwardingResultId = AdvancedResultExtensions.getForwardingInstructionId(openInstruction)
            ?: return instruction

        val containers = context.rootContext()
            .containerManager
            .containers
            .plus(ActivityNavigationContainer(context.activity.navigationContext))
            .toMutableList()

        while (containers.isNotEmpty()) {
            val next = containers.removeAt(0)
            val filteredBackstack = next.backstack
                .filterNot {
                    (it.instructionId == forwardingResultId && it.internal.resultKey !is FlowStep<*>) ||
                        AdvancedResultExtensions.getForwardingInstructionId(it) == forwardingResultId
                }
                .toBackstack()

            if (filteredBackstack.size != next.backstack.size) {
                next.setBackstack(filteredBackstack)
            }
            containers.addAll(next.childContext?.containerManager?.containers.orEmpty())
        }

        val addPendingResult = context.controller.dependencyScope.get<AddPendingResult>()
        addPendingResult(
            context,
            instruction,
        )
        return null
    }
}