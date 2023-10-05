package dev.enro.core.controller.usecase

import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.result.EnroResult
import dev.enro.core.result.UnmanagedNavigationResultChannel
import dev.enro.core.result.internal.ResultChannelImpl
import kotlin.reflect.KClass

@PublishedApi
internal val NavigationHandle.createResultChannel: CreateResultChannel
    get() = dependencyScope.get()

@PublishedApi
internal class CreateResultChannel(
    private val navigationHandle: NavigationHandle,
    private val enroResult: EnroResult,
) {
    // Inlining is important here to ensure uniqueness of generated lambda names,
    // which are used as part of the identity of the result channels
    inline operator fun <Result : Any, Key : NavigationKey.WithResult<Result>> invoke(
        resultType: KClass<Result>,
        crossinline onClosed: () -> Unit,
        crossinline onResult: (Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedNavigationResultChannel<Result, Key> {
        return create(
            resultType = resultType,
            onClosed = { _ -> onClosed() },
            onResult = { _, result -> onResult(result) },
            additionalResultId = additionalResultId,
        )
    }

    // Inlining is important here to ensure uniqueness of generated lambda names,
    // which are used as part of the identity of the result channels
    inline operator fun <Result : Any, Key : NavigationKey.WithResult<Result>> invoke(
        resultType: KClass<Result>,
        crossinline onClosed: (Key) -> Unit,
        crossinline onResult: (Key, Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedNavigationResultChannel<Result, Key> {
        return create(
            resultType = resultType,
            onClosed = { key -> onClosed(key) },
            onResult = { key, result -> onResult(key, result) },
            additionalResultId = additionalResultId,
        )
    }

    @PublishedApi
    internal fun <Result : Any, Key : NavigationKey.WithResult<Result>> create(
        resultType: KClass<Result>,
        onClosed: (Key) -> Unit,
        onResult: (Key, Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedNavigationResultChannel<Result, Key> {
        return ResultChannelImpl(
            enroResult = enroResult,
            navigationHandle = navigationHandle,
            resultType = resultType.java,
            onClosed = onClosed,
            onResult = onResult,
            additionalResultId = additionalResultId,
        )
    }
}