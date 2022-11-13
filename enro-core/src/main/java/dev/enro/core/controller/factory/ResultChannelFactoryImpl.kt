package dev.enro.core.controller.factory

import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.result.UnmanagedEnroResultChannel
import dev.enro.core.result.internal.EnroResult
import dev.enro.core.result.internal.ResultChannelImpl
import dev.enro.core.usecase.ResultChannelFactory
import kotlin.reflect.KClass

@PublishedApi
internal class ResultChannelFactoryImpl(
    private val navigationHandle: NavigationHandle,
    private val enroResult: EnroResult,
) :ResultChannelFactory {
    override fun <Result: Any, Key: NavigationKey.WithResult<Result>> createResultChannel(
        resultType: KClass<Result>,
        onResult: (Result) -> Unit,
        additionalResultId: String,
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