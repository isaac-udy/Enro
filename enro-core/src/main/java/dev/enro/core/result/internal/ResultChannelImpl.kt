package dev.enro.core.result.internal

import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.internal.handle.context
import dev.enro.core.result.EnroResult
import dev.enro.core.result.EnroResultChannel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResultForwardingInfo(
    val from: Class<out Any>,
    val fromId: String
): Parcelable

class ResultChannelImpl<T> internal constructor(
    private val navigationHandle: NavigationHandle,
    private val resultType: Class<T>,
    private val isForwarding: Boolean,
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

    override fun open(key: NavigationKey.WithResult<T>) {
        val existingForwardId = if(isForwarding) getResultId(navigationHandle) else null

        navigationHandle.executeInstruction(
            NavigationInstruction.Forward(key).apply {
                additionalData.apply {
                    putParcelable(EXTRA_RESULT_CHANNEL_ID, existingForwardId ?: id)
                    putBoolean(EXTRA_RESULT_IS_FORWARDING, existingForwardId != null)

                    if(existingForwardId != null) {
                        putParcelableArrayList(EXTRA_RESULT_FORWARDING_DATA, ArrayList(getForwardingData(navigationHandle) + ResultForwardingInfo(
                            from = navigationHandle.context.contextReference::class.java,
                            fromId = navigationHandle.id
                        )))
                    } else {
                        putParcelableArrayList(EXTRA_RESULT_FORWARDING_DATA, arrayListOf(ResultForwardingInfo(
                            from = navigationHandle.context.contextReference::class.java,
                            fromId = navigationHandle.id
                        )))
                    }
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
        private const val EXTRA_RESULT_CHANNEL_ID = "com.enro.core.RESULT_CHANNEL_ID"
        private const val EXTRA_RESULT_IS_FORWARDING = "com.enro.core.RESULT_IS_FORWARDING"
        private const val EXTRA_RESULT_FORWARDING_DATA = "com.enro.core.RESULT_FORWARDING_DATA"

        internal fun getResultId(navigationHandle: NavigationHandle): ResultChannelId? {
            val classLoader = navigationHandle.additionalData.classLoader
            navigationHandle.additionalData.classLoader = ResultChannelId::class.java.classLoader
            val resultId = navigationHandle.additionalData.getParcelable<ResultChannelId>(
                EXTRA_RESULT_CHANNEL_ID
            )
            navigationHandle.additionalData.classLoader = classLoader
            return resultId
        }

        internal fun isForwardingResult(navigationHandle: NavigationHandle): Boolean {
            val classLoader = navigationHandle.additionalData.classLoader
            navigationHandle.additionalData.classLoader = ResultChannelId::class.java.classLoader

            val resultId = navigationHandle.additionalData.getParcelable<ResultChannelId>(
                EXTRA_RESULT_CHANNEL_ID
            ) ?: return false

            val enroResult = EnroResult.from(navigationHandle.controller)
            navigationHandle.additionalData.classLoader = classLoader
            return enroResult.hasPendingResult(resultId)
        }

        internal fun getForwardingData(navigationHandle: NavigationHandle): List<ResultForwardingInfo> {
            val classLoader = navigationHandle.additionalData.classLoader
            navigationHandle.additionalData.classLoader = ResultChannelId::class.java.classLoader
            val resultId = navigationHandle.additionalData.getParcelableArrayList<ResultForwardingInfo>(
                EXTRA_RESULT_FORWARDING_DATA
            ).orEmpty()
            navigationHandle.additionalData.classLoader = classLoader
            return resultId
        }
    }
}

