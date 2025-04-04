package dev.enro.core.controller.usecase

import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.result.EnroResult
import dev.enro.core.result.NavigationResultScope
import dev.enro.core.result.UnmanagedNavigationResultChannel
import dev.enro.core.result.internal.ResultChannelImpl
import kotlin.reflect.KClass

@PublishedApi
internal val NavigationHandle.createResultChannel: CreateResultChannel
    get() = dependencyScope.get()

@PublishedApi
internal class CreateResultChannel(
    @PublishedApi internal val navigationHandle: NavigationHandle,
    @PublishedApi internal val enroResult: EnroResult,
) {
    operator fun <Result : Any, Key : NavigationKey.WithResult<Result>> invoke(
        resultType: KClass<Result>,
        resultId: String,
        onClosed: NavigationResultScope<Result, Key>.() -> Unit,
        onResult: NavigationResultScope<Result, Key>.(Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedNavigationResultChannel<Result, Key> {
        return create(
            resultType = resultType,
            resultId = resultId,
            onClosed = onClosed,
            onResult = onResult,
            additionalResultId = additionalResultId,
        )
    }

    @PublishedApi
    internal fun <Result : Any, Key : NavigationKey.WithResult<Result>> create(
        resultType: KClass<Result>,
        resultId: String,
        onClosed: NavigationResultScope<Result, Key>.() -> Unit,
        onResult: NavigationResultScope<Result, Key>.(Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedNavigationResultChannel<Result, Key> {
        return ResultChannelImpl(
            enroResult = enroResult,
            navigationHandle = navigationHandle,
            resultType = resultType,
            onClosed = onClosed,
            onResult = onResult,
            resultId = resultId,
            additionalResultId = additionalResultId,
        )
    }
}