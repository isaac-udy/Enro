package dev.enro.core.controller.interceptor.builder

import dev.enro.core.*
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.usecase.AddPendingResult

public sealed class OnNavigationKeyClosedWithResultScope {
    public fun cancelResult(): InterceptorBehavior.Cancel =
        InterceptorBehavior.Cancel()

    public fun deliverResultAndCancelClose(): InterceptorBehavior.DeliverResultAndCancel =
        InterceptorBehavior.DeliverResultAndCancel()

    public fun continueWithClose(): InterceptorBehavior.Continue =
        InterceptorBehavior.Continue()

    public fun replaceCloseWith(instruction: AnyOpenInstruction): InterceptorBehavior.ReplaceWith =
        InterceptorBehavior.ReplaceWith(instruction)
}

@PublishedApi
internal class OnNavigationKeyClosedWithResultInterceptor<T : Any>(
    private val matcher: (NavigationKey) -> Boolean,
    private val action: OnNavigationKeyClosedWithResultScope.(NavigationKey, T) -> InterceptorBehavior.ForResult
) : OnNavigationKeyClosedWithResultScope(), NavigationInstructionInterceptor {
    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>,
    ): NavigationInstruction? {
        if (instruction !is NavigationInstruction.Close.WithResult) return instruction
        val openInstruction = context.arguments.readOpenInstruction() ?: return instruction
        if (!matcher(openInstruction.navigationKey)) return openInstruction
        val addPendingResult = context.controller.dependencyScope.get<AddPendingResult>()

        // This should be checked by reified types when this interceptor is constructed
        @Suppress("UNCHECKED_CAST")
        val result = action(openInstruction.navigationKey, instruction.result as T)

        return when (result) {
            is InterceptorBehavior.Cancel -> null
            is InterceptorBehavior.Continue -> instruction
            is InterceptorBehavior.ReplaceWith -> result.instruction
            is InterceptorBehavior.DeliverResultAndCancel -> {
                addPendingResult.invoke(
                    navigationContext = context,
                    instruction = instruction,
                )
                null
            }
        }
    }
}