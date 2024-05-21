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
    /**
     * The resultId being set here to the JVM class name of the onResult lambda is a key part of
     * being able to make result channels work without providing an explicit id. The JVM will treat
     * the lambda as an anonymous class, which is uniquely identifiable by it's class name.
     *
     * If the behaviour of the Kotlin/JVM interaction changes in a future release, it may be required
     * to pass an explicit resultId as a part of the ResultChannelImpl constructor, which would need
     * to be unique per result channel created.
     *
     * It is possible to have two result channels registered for the same result type:
     * <code>
     *     val resultOne = registerForResult<Boolean> { ... }
     *     val resultTwo = registerForResult<Boolean> { ... }
     *
     *     // ...
     *     resultTwo.open(SomeNavigationKey( ... ))
     * </code>
     *
     * It's important in this case that resultTwo can be identified as the channel to deliver the
     * result into, and this identification needs to be stable across application process death.
     * The simple solution would be to require users to provide a name for the channel:
     * <code>
     *     val resultTwo = registerForResult<Boolean>("resultTwo") { ... }
     * </code>
     *
     * but using the anonymous class name is a nicer way to do things for now, with the ability to
     * fall back to explicit identification of the channels in the case that the Kotlin/JVM behaviour
     * changes in the future.
     */
    fun Any.createResultId(): String {
        return this::class.java.name
    }

    // It is important that these functions are inlined, so that the empty lambda can be used
    // as a part of the result id, which is helpful for providing uniqueness related to location in
    // the code
    @Deprecated("Use the other overload of invoke, as key is provided through the NavigationResultScope and doesn't need to be passed as a parameter")
    inline operator fun <Result : Any, Key : NavigationKey.WithResult<Result>> invoke(
        resultType: KClass<Result>,
        crossinline onClosed: NavigationResultScope<Result, Key>.(Key) -> Unit,
        noinline onResult: NavigationResultScope<Result, Key>.(Key, Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedNavigationResultChannel<Result, Key> {
        val internalOnClosed: NavigationResultScope<Result, Key>.() -> Unit = { onClosed(key) }
        val internalOnResult: NavigationResultScope<Result, Key>.(Result) -> Unit = { result -> onResult(key, result) }
        val resultId = onResult.createResultId() + "@" + internalOnResult.createResultId().hashCode()
        return create(
            resultType = resultType,
            resultId = resultId,
            onClosed = internalOnClosed,
            onResult = internalOnResult,
            additionalResultId = additionalResultId,
        )
    }

    // It is important that these functions are inlined, so that the empty lambda can be used
    // as a part of the result id, which is helpful for providing uniqueness related to location in
    // the code
    inline operator fun <Result : Any, Key : NavigationKey.WithResult<Result>> invoke(
        resultType: KClass<Result>,
        crossinline onClosed: NavigationResultScope<Result, Key>.() -> Unit,
        noinline onResult: NavigationResultScope<Result, Key>.(Result) -> Unit,
        additionalResultId: String = "",
    ): UnmanagedNavigationResultChannel<Result, Key> {
        val internalOnClosed: NavigationResultScope<Result, Key>.() -> Unit = { onClosed(this) }
        val internalOnResult: NavigationResultScope<Result, Key>.(Result) -> Unit = { result -> onResult(this, result) }
        val resultId = onResult.createResultId() + "@" + internalOnResult.createResultId().hashCode()
        return create(
            resultType = resultType,
            resultId = resultId,
            onClosed = internalOnClosed,
            onResult = internalOnResult,
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
            resultType = resultType.java,
            onClosed = onClosed,
            onResult = onResult,
            resultId = resultId,
            additionalResultId = additionalResultId,
        )
    }
}