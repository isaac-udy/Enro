package nav.enro.result.internal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import nav.enro.core.NavigationDirection
import nav.enro.core.NavigationHandle
import nav.enro.core.NavigationInstruction
import nav.enro.core.TypedNavigationHandle
import nav.enro.result.EnroResult
import nav.enro.result.EnroResultChannel
import nav.enro.result.ResultNavigationKey

class ResultChannelImpl<T> internal constructor(
    private val navigationHandle: NavigationHandle,
    private val resultType: Class<T>,
    private val onResult: (T) -> Unit
): EnroResultChannel<T> {
    internal val id = ResultChannelId(
        ownerId = navigationHandle.id,
        resultId = onResult::class.java.name
    )

    init {
        EnroResult.from(navigationHandle.controller).registerChannel(this)
    }

    override fun open(key: ResultNavigationKey<T>) {
        navigationHandle.executeInstruction(
            NavigationInstruction.Open(
                navigationDirection = NavigationDirection.FORWARD,
                navigationKey = key,
                additionalData = Bundle().apply {
                    putParcelable(EXTRA_RESULT_CHANNEL_ID, id)
                }
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    internal fun consumeResult(result: Any) {
        if(!resultType.isAssignableFrom(result::class.java))
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

        internal fun getResultId(navigationHandle: NavigationHandle): ResultChannelId? {
            val classLoader = navigationHandle.additionalData.classLoader
            navigationHandle.additionalData.classLoader = ResultChannelId::class.java.classLoader
            val resultId = navigationHandle.additionalData.getParcelable<ResultChannelId>(
                EXTRA_RESULT_CHANNEL_ID
            )
            navigationHandle.additionalData.classLoader = classLoader
            return resultId
        }

        internal fun getResultId(navigationHandle: TypedNavigationHandle<*>): ResultChannelId? {
            val classLoader = navigationHandle.additionalData.classLoader
            navigationHandle.additionalData.classLoader = ResultChannelId::class.java.classLoader
            val resultId = navigationHandle.additionalData.getParcelable<ResultChannelId>(
                EXTRA_RESULT_CHANNEL_ID
            )
            navigationHandle.additionalData.classLoader = classLoader
            return resultId
        }
    }
}

