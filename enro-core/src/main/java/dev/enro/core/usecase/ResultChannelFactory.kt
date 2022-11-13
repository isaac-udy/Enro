package dev.enro.core.usecase

import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.get
import dev.enro.core.result.UnmanagedEnroResultChannel
import kotlin.reflect.KClass

@PublishedApi
internal val NavigationHandle.resultChannelFactory: ResultChannelFactory
    get() = dependencyScope.get()

@PublishedApi
internal interface ResultChannelFactory {
    fun <Result: Any, Key: NavigationKey.WithResult<Result>> createResultChannel(
        resultType: KClass<Result>,
        onResult: (Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedEnroResultChannel<Result, Key>
}