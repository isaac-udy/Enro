package dev.enro.interceptor.builder

import dev.enro.NavigationOperation

// The scope functions of an interceptor block throw an InterceptorBuilderResult to end the
// block. runForInterceptorBuilderResult catches the InterceptorBuilderResult.
@PublishedApi
internal sealed class InterceptorBuilderResult : RuntimeException() {
    class Continue : InterceptorBuilderResult()

    class Cancel : InterceptorBuilderResult()

    class CancelAnd(val block: () -> Unit) : InterceptorBuilderResult()

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