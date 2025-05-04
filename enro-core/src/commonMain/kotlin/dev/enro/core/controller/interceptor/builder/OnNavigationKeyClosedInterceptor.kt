package dev.enro.core.controller.interceptor.builder

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor

public sealed class OnNavigationKeyClosedScope {
    /**
     * Cancel the close instruction, preventing the destination from being closed.
     */
    public fun cancelClose(): InterceptorBehavior.Cancel =
        InterceptorBehavior.Cancel()

    /**
     * Allow the close instruction to continue as normal.
     */
    public fun continueWithClose(): InterceptorBehavior.Continue =
        InterceptorBehavior.Continue()

    /**
     * Cancel the close instruction and instead execute the provide NavigationInstruction.Open
     */
    public fun replaceCloseWith(instruction: AnyOpenInstruction): InterceptorBehavior.ReplaceWith =
        InterceptorBehavior.ReplaceWith(instruction)
}

@PublishedApi
internal class OnNavigationKeyClosedInterceptor(
    private val matcher: (NavigationKey) -> Boolean,
    private val action: OnNavigationKeyClosedScope.(NavigationKey) -> InterceptorBehavior.ForClose
) : OnNavigationKeyClosedScope(), NavigationInstructionInterceptor {
    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>,
    ): NavigationInstruction? {
        val openInstruction = context.instruction
        if (!matcher(openInstruction.navigationKey)) return openInstruction
        val result = action(openInstruction.navigationKey)
        return when (result) {
            is InterceptorBehavior.Cancel -> null
            is InterceptorBehavior.Continue -> instruction
            is InterceptorBehavior.ReplaceWith -> result.instruction
        }
    }
}