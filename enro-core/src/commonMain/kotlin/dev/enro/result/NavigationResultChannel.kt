package dev.enro.result

import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.platform.EnroLog
import dev.enro.result.NavigationResult.Completed.Companion.result
import dev.enro.result.NavigationResultChannel.ResultIdKey
import dev.enro.withMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

public class NavigationResultChannel<Result : Any> @PublishedApi internal constructor(
    @PublishedApi
    internal val id: Id,
    @PublishedApi
    internal val navigationHandle: NavigationHandle<*>,
    @PublishedApi
    internal val onClosed: NavigationResultScope<NavigationKey>.() -> Unit,
    @PublishedApi
    internal val onCompleted: NavigationResultScope<NavigationKey>.(Result) -> Unit,
) {
    @Serializable
    public data class Id(
        val ownerId: String,
        val resultId: String
    )

    internal object ResultIdKey : NavigationKey.MetadataKey<Id?>(null)

    //    @PublishedApi
    public companion object {
        //        @PublishedApi
        public val pendingResults: MutableStateFlow<Map<Id, NavigationResult<*>>> = MutableStateFlow(emptyMap())

        @PublishedApi
        internal val activeChannels: MutableSet<Id> = mutableSetOf()

        @PublishedApi
        internal inline fun <reified T : Any> observe(
            scope: CoroutineScope,
            resultChannel: NavigationResultChannel<T>,
        ): Job {
            return observe(T::class, scope, resultChannel)
        }

        @PublishedApi
        internal fun <T : Any> observe(
            resultType: KClass<T>,
            scope: CoroutineScope,
            resultChannel: NavigationResultChannel<T>,
        ): Job {
            return pendingResults
                .onStart {
                    require(!activeChannels.contains(resultChannel.id)) {
                        "NavigationResultChannel with id ${resultChannel.id} is already being observed"
                    }
                    activeChannels.add(resultChannel.id)
                }
                .map { pendingResults ->
                    resultChannel.id to pendingResults[resultChannel.id]
                }
                .distinctUntilChanged()
                .onEach { (id, result) ->
                    if (result == null) return@onEach

                    when (result) {
                        is NavigationResult.Delegated -> {}
                        is NavigationResult.Closed -> resultChannel.onClosed(NavigationResultScope(result.instance))
                        is NavigationResult.Completed -> {
                            if (resultType == Unit::class) {
                                resultChannel.onCompleted(NavigationResultScope(result.instance), Unit as T)
                            } else {
                                @Suppress("UNCHECKED_CAST")
                                result as NavigationResult.Completed<out NavigationKey.WithResult<T>>
                                resultChannel.onCompleted(NavigationResultScope(result.instance), result.result)
                            }
                        }
                    }
                    pendingResults.value -= id
                }
                .onCompletion {
                    activeChannels.remove(resultChannel.id)
                }
                .launchIn(scope)
        }

        internal fun registerResult(
            result: NavigationResult<NavigationKey>,
        ) {
            val resultId = result.instance.metadata.get(ResultIdKey)
            // If the NavigationKey.Instance does not have a value for ResultIdKey,
            // then there is no NavigationResultChannel that is waiting for results
            // from that NavigationKey.Instance, and we won't register the result
            if (resultId == null) {
                return
            }
            pendingResults.value += resultId to result
        }

        internal fun hasCompletedResultFor(
            instance: NavigationKey.Instance<*>,
        ): Boolean {
            val resultId = instance.metadata.get(ResultIdKey)
            val pendingResults = pendingResults.value
            return resultId != null && pendingResults[resultId] is NavigationResult.Completed<*>
        }

        // Returns a flow that will emit a single Unit value whenever a
        // NavigationResult.Completed is registered for the resultId associated with the
        // NavigationKey.Instance passed as a parameter, but only if the result did not
        // come from the instance itself (i.e. it launched another destination
        // as completeFrom, and that destination returned a result)
        internal fun completedFromSignalFor(
            instance: NavigationKey.Instance<*>,
        ): Flow<Unit> {
            val resultId = instance.metadata.get(ResultIdKey)
            if (resultId == null) return emptyFlow()
            return pendingResults
                .mapNotNull { pendingResults ->
                    pendingResults[resultId]
                }
                .distinctUntilChanged()
                .filterIsInstance<NavigationResult.Completed<NavigationKey>>()
                .distinctUntilChanged()
                .filter {
                    it.instance.id != instance.id
                }
                .map { }
                .take(1)
        }
    }
}

public fun <Result : Any> NavigationResultChannel<Result>.open(key: NavigationKey.WithResult<out Result>) {
    navigationHandle.execute(
        operation = NavigationOperation.Open(
            instance = key.withMetadata(ResultIdKey, id).asInstance()
        )
    )
}

public fun <Result : Any> NavigationResultChannel<Result>.open(key: NavigationKey.WithMetadata<out NavigationKey.WithResult<out Result>>) {
    navigationHandle.execute(
        operation = NavigationOperation.Open(
            instance = key.withMetadata(ResultIdKey, id).asInstance()
        )
    )
}

@JvmName("openAny")
public fun NavigationResultChannel<Unit>.open(key: NavigationKey) {
    navigationHandle.execute(
        operation = NavigationOperation.Open(
            instance = key.withMetadata(ResultIdKey, id).asInstance()
        )
    )
}

@JvmName("openAny")
public fun NavigationResultChannel<Unit>.open(key: NavigationKey.WithMetadata<*>) {
    navigationHandle.execute(
        operation = NavigationOperation.Open(
            instance = key.withMetadata(ResultIdKey, id).asInstance()
        )
    )
}

public class NavigationResultScope<Key : NavigationKey> @PublishedApi internal constructor(
    public val instance: NavigationKey.Instance<Key>,
) {
    public val key: Key get() = instance.key
}
