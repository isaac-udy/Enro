package dev.enro.core.controller.interceptor.builder

import dev.enro.core.NavigationInstruction

public sealed class InterceptorBehavior {
    public object Cancel: InterceptorBehavior()
    public object Continue: InterceptorBehavior()
    public class ReplaceWith(
        public val instruction: NavigationInstruction.Open<*>,
    ): InterceptorBehavior()
}