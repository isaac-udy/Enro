package dev.enro.core.result.internal

import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.enro.core.*
import dev.enro.core.result.EnroResult
import dev.enro.core.result.UnmanagedEnroResultChannel

private class ResultChannelProperties<T>(
    val navigationHandle: NavigationHandle,
    val resultType: Class<T>,
    val onResult: (T) -> Unit,
)

class ResultChannelImpl<Result, Key: NavigationKey.WithResult<Result>> @PublishedApi internal constructor(
    navigationHandle: NavigationHandle,
    resultType: Class<Result>,
    onResult: (Result) -> Unit,
    additionalResultId: String = "",
) : UnmanagedEnroResultChannel<Result, Key> {

    /**
     * The arguments passed to the ResultChannelImpl hold references to the external world, and
     * can hold references to objects that could leak in memory. We store these properties inside
     * a variable which is cleared to null when the ResultChannelImpl is destroyed, to ensure
     * that these references are not held by the ResultChannelImpl after it has been destroyed.
     */
    private var arguments: ResultChannelProperties<Result>? = ResultChannelProperties(
        navigationHandle = navigationHandle,
        resultType = resultType,
        onResult = onResult,
    )

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
    internal val id = ResultChannelId(
        ownerId = navigationHandle.id,
        resultId = onResult::class.java.name +"@"+additionalResultId
    )

    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        if(event == Lifecycle.Event.ON_DESTROY) {
            destroy()
        }
    }.apply { navigationHandle.lifecycle.addObserver(this) }

    override fun open(key: Key) {
        val properties = arguments ?: return
        properties.navigationHandle.executeInstruction(
            NavigationInstruction.Forward(key).internal.copy(
                resultId = id
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    internal fun consumeResult(result: Any) {
        val properties = arguments ?: return
        if (!properties.resultType.isAssignableFrom(result::class.java))
            throw EnroException.ReceivedIncorrectlyTypedResult("Attempted to consume result with wrong type!")
        result as Result
        properties.navigationHandle.runWhenHandleActive {
            properties.onResult(result)
        }
    }

    override fun attach() {
        val properties = arguments ?: return
        if(properties.navigationHandle.lifecycle.currentState == Lifecycle.State.DESTROYED) return
        EnroResult.from(properties.navigationHandle.controller)
            .registerChannel(this)
    }

    override fun detach() {
        val properties = arguments ?: return
        EnroResult.from(properties.navigationHandle.controller)
            .deregisterChannel(this)
    }

    override fun destroy() {
        val properties = arguments ?: return
        detach()
        properties.navigationHandle.lifecycle.removeObserver(lifecycleObserver)
        arguments = null
    }

    internal companion object {
        internal fun getResultId(navigationHandle: NavigationHandle): ResultChannelId? {
            return navigationHandle.instruction.internal.resultId
        }

        internal fun getResultId(instruction: NavigationInstruction.Open): ResultChannelId? {
            return instruction.internal.resultId
        }

        internal fun overrideResultId(instruction: NavigationInstruction.Open, resultId: ResultChannelId): NavigationInstruction.Open {
            return instruction.internal.copy(
                resultId = resultId
            )
        }
    }
}

// Used reflectively by ResultExtensions in enro-test
@Keep
private fun getResultId(navigationInstruction: NavigationInstruction.Open): ResultChannelId? {
    return navigationInstruction.internal.resultId
}