package dev.enro.core.interceptor.builder

import dev.enro.core.*
import dev.enro.core.interceptor.NavigationInstructionInterceptor

public sealed class OnNavigationKeyClosedScope {
    public fun cancelClose(): InterceptorBehavior.Cancel =
        InterceptorBehavior.Cancel()

    public fun continueWithClose(): InterceptorBehavior.Continue =
        InterceptorBehavior.Continue()

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
        val openInstruction = context.arguments.readOpenInstruction() ?: return instruction
        if (!matcher(openInstruction.navigationKey)) return openInstruction
        val result = action(openInstruction.navigationKey)
        return when (result) {
            is InterceptorBehavior.Cancel -> null
            is InterceptorBehavior.Continue -> instruction
            is InterceptorBehavior.ReplaceWith -> result.instruction
        }
    }
}