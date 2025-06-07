package dev.enro.interceptor.builder

import dev.enro.NavigationOperation

/**
 * Represents the action to take when intercepting a navigation transition.
 */
@PublishedApi
internal sealed class InterceptorBuilderResult : RuntimeException() {
    /**
     * Continue with the original navigation transition.
     */
    class Continue : InterceptorBuilderResult()

    /**
     * Cancel the navigation transition entirely.
     */
    class Cancel : InterceptorBuilderResult()

    /**
     * Cancel the navigation transition and execute a block of code after the transition is cancelled.
     */
    class CancelAnd(val block: () -> Unit) : InterceptorBuilderResult()

    /**
     * Replace the current transition with a modified one.
     */
    class ReplaceWith(
        val operation: NavigationOperation,
    ) : InterceptorBuilderResult()
}

internal fun runForInterceptorBuilderResult(block: () -> Unit): InterceptorBuilderResult {
    return try {
        block()
        return InterceptorBuilderResult.Continue()
    } catch (interceptorResult: InterceptorBuilderResult) {
        interceptorResult
    }
}