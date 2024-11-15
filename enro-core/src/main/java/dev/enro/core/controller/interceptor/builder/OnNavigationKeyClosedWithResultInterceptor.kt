package dev.enro.core.controller.interceptor.builder

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.usecase.AddPendingResult
import dev.enro.core.readOpenInstruction

public sealed class OnNavigationKeyClosedWithResultScope {
    /**
     * Cancel the close instruction, preventing the destination from being closed, and cancel the result being delivered.
     */
    public fun cancelCloseAndResult(): InterceptorBehavior.Cancel =
        InterceptorBehavior.Cancel()

    /**
     * Cancel the close instruction, preventing the destination from being closed, but allow the result to be delivered.
     */
    public fun deliverResultAndCancelClose(): InterceptorBehavior.DeliverResultAndCancel =
        InterceptorBehavior.DeliverResultAndCancel()

    /**
     * Allow the close instruction to execute as normal, and the result to be delivered as normal.
     */
    public fun continueWithClose(): InterceptorBehavior.Continue =
        InterceptorBehavior.Continue()

    /**
     * Cancel the close instruction and prevent the result being delivered, and instead execute the provided NavigationInstruction.Open
     */
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