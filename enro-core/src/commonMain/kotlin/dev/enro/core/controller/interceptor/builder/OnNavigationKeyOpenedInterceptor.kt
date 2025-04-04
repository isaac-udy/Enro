package dev.enro.core.controller.interceptor.builder

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor

public sealed class OnNavigationKeyOpenedScope {
    /**
     * Cancel the open instruction, preventing the destination from being opened.
     */
    public fun cancelNavigation(): InterceptorBehavior.Cancel =
        InterceptorBehavior.Cancel()

    /**
     * Allow the open instruction to continue as normal.
     */
    public fun continueWithNavigation(): InterceptorBehavior.Continue =
        InterceptorBehavior.Continue()

    /**
     * Cancel the open instruction and instead execute the provided NavigationInstruction.Open
     */
    public fun replaceNavigationWith(instruction: AnyOpenInstruction): InterceptorBehavior.ReplaceWith =
        InterceptorBehavior.ReplaceWith(instruction)
}

@PublishedApi
internal class OnNavigationKeyOpenedInterceptor(
    private val matcher: (NavigationKey) -> Boolean,
    private val action: OnNavigationKeyOpenedScope.(NavigationKey) -> InterceptorBehavior.ForOpen
) : OnNavigationKeyOpenedScope(), NavigationInstructionInterceptor {
    override fun intercept(
        instruction: AnyOpenInstruction,
        context: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction? {
        if (!matcher(instruction.navigationKey)) return instruction
        val result = action(instruction.navigationKey)
        return when (result) {
            is InterceptorBehavior.Cancel -> null
            is InterceptorBehavior.Continue -> instruction
            is InterceptorBehavior.ReplaceWith -> result.instruction
        }
    }
}
