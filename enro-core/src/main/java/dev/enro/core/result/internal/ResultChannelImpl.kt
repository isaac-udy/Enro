package dev.enro.core.result.internal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.result.EnroResult
import dev.enro.core.result.EnroResultChannel

private const val EXTRA_RESULT_CHANNEL_ID = "com.enro.core.RESULT_CHANNEL_ID"

class ResultChannelImpl<T> @PublishedApi internal  constructor(
    private val navigationHandle: NavigationHandle,
    private val resultType: Class<T>,
    private val onResult: (T) -> Unit
) : EnroResultChannel<T> {
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
        resultId = onResult::class.java.name
    )

    init {
        EnroResult.from(navigationHandle.controller).registerChannel(this)
    }

    override fun open(key: NavigationKey.WithResult<T>) {
        navigationHandle.executeInstruction(
            NavigationInstruction.Forward(key).apply {
                additionalData.apply {
                    putParcelable(EXTRA_RESULT_CHANNEL_ID, id)
                }
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    internal fun consumeResult(result: Any) {
        if (!resultType.isAssignableFrom(result::class.java))
            throw IllegalArgumentException("Attempted to consume result with wrong type!")
        result as T

        handler.post {
            navigationHandle.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                fun onResume() {
                    onResult(result)
                    navigationHandle.lifecycle.removeObserver(this)
                }
            })
        }
    }

    internal companion object {
        private val handler = Handler(Looper.getMainLooper())
        internal fun getResultId(navigationHandle: NavigationHandle): ResultChannelId? {
            return getResultId(navigationHandle.additionalData)
        }
    }
}

// Used reflectively by ResultExtensions in enro-test
@Keep
private fun getResultId(bundle: Bundle): ResultChannelId? {
    val classLoader = bundle.classLoader
    bundle.classLoader = ResultChannelId::class.java.classLoader
    val resultId = bundle.getParcelable<ResultChannelId>(
        EXTRA_RESULT_CHANNEL_ID
    )
    bundle.classLoader = classLoader
    return resultId
}