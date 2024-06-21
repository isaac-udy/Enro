package dev.enro.core.result.internal

import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.result.EnroResult
import dev.enro.core.result.NavigationResultScope
import dev.enro.core.result.UnmanagedNavigationResultChannel
import dev.enro.core.runWhenHandleActive

private class ResultChannelProperties<Result : Any, Key : NavigationKey.WithResult<Result>>(
    val navigationHandle: NavigationHandle,
    val resultType: Class<Result>,
    val onClosed: NavigationResultScope<Result, Key>.() -> Unit,
    val onResult: NavigationResultScope<Result, Key>.(Result) -> Unit,
)

@PublishedApi
internal class ResultChannelImpl<Result : Any, Key : NavigationKey.WithResult<Result>>(
    private val enroResult: EnroResult,
    navigationHandle: NavigationHandle,
    resultType: Class<Result>,
    onClosed: @DisallowComposableCalls NavigationResultScope<Result, Key>.() -> Unit,
    onResult: @DisallowComposableCalls NavigationResultScope<Result, Key>.(Result) -> Unit,
    resultId: String,
    additionalResultId: String = "",
) : UnmanagedNavigationResultChannel<Result, Key> {

    /**
     * The arguments passed to the ResultChannelImpl hold references to the external world, and
     * can hold references to objects that could leak in memory. We store these properties inside
     * a variable which is cleared to null when the ResultChannelImpl is destroyed, to ensure
     * that these references are not held by the ResultChannelImpl after it has been destroyed.
     */
    private var arguments: ResultChannelProperties<Result, Key>? = ResultChannelProperties(
        navigationHandle = navigationHandle,
        resultType = resultType,
        onClosed = onClosed,
        onResult = onResult,
    )

    internal val id = ResultChannelId(
        ownerId = navigationHandle.id,
        resultId = resultId.let { resultId ->
            when {
                additionalResultId.isEmpty() -> return@let resultId
                else -> "$resultId ($additionalResultId)"
            }
        }
    )

    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
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

    override fun push(key: NavigationKey.SupportsPush.WithResult<out Result>) {
        val properties = arguments ?: return
        properties.navigationHandle.executeInstruction(
            NavigationInstruction.Push(key).internal.copy(
                resultId = id
            )
        )
    }

    override fun push(key: NavigationKey.WithExtras<out NavigationKey.SupportsPush.WithResult<out Result>>) {
        val properties = arguments ?: return
        properties.navigationHandle.executeInstruction(
            NavigationInstruction.Push(key).internal.copy(
                resultId = id
            )
        )
    }

    override fun present(key: NavigationKey.SupportsPresent.WithResult<out Result>) {
        val properties = arguments ?: return
        properties.navigationHandle.executeInstruction(
            NavigationInstruction.Present(key).internal.copy(
                resultId = id
            )
        )
    }

    override fun present(key: NavigationKey.WithExtras<out NavigationKey.SupportsPresent.WithResult<out Result>>) {
        val properties = arguments ?: return
        properties.navigationHandle.executeInstruction(
            NavigationInstruction.Present(key).internal.copy(
                resultId = id
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    internal fun consumeResult(pendingResult: PendingResult) {
        val properties = arguments ?: return
        when (pendingResult) {
            is PendingResult.Closed -> {
                val key = pendingResult.navigationKey
                key as Key
                properties.navigationHandle.runWhenHandleActive {
                    properties.onClosed(NavigationResultScope(pendingResult.instruction, key))
                }
            }

            is PendingResult.Result -> {
                val result = pendingResult.result
                val key = pendingResult.navigationKey
                if (!properties.resultType.isAssignableFrom(result::class.java))
                    throw EnroException.ReceivedIncorrectlyTypedResult("Attempted to consume result with wrong type; expended ${properties.resultType.simpleName} but was ${result::class.java.simpleName}")
                result as Result
                key as Key
                properties.navigationHandle.runWhenHandleActive {
                    properties.onResult(NavigationResultScope(pendingResult.instruction, key), result)
                }
            }
        }
    }

    override fun attach() {
        val properties = arguments ?: return
        if (properties.navigationHandle.lifecycle.currentState == Lifecycle.State.DESTROYED) return
        enroResult.registerChannel(this)
    }

    override fun detach() {
        val properties = arguments ?: return
        enroResult.deregisterChannel(this)
    }

    override fun destroy() {
        val properties = arguments ?: return
        detach()
        properties.navigationHandle.lifecycle.removeObserver(lifecycleObserver)
        arguments = null
    }
}
