package dev.enro.core.controller.usecase

import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.result.EnroResult
import dev.enro.core.result.UnmanagedEnroResultChannel
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
    operator fun <Result: Any, Key: NavigationKey.WithResult<Result>> invoke(
        resultType: KClass<Result>,
        onResult: (Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedEnroResultChannel<Result, Key> {
        return ResultChannelImpl(
            enroResult = enroResult,
            navigationHandle = navigationHandle,
            resultType = resultType.java,
            onResult = onResult,
            additionalResultId = additionalResultId,
        )
    }
}