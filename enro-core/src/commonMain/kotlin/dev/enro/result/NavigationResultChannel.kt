package dev.enro.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.getNavigationHandle
import dev.enro.result.NavigationResult.Completed.Companion.result
import dev.enro.result.NavigationResultChannel.ResultIdKey
import dev.enro.ui.LocalNavigationHandle
import dev.enro.withMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName
import kotlin.properties.ReadOnlyProperty
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
    @PublishedApi
    internal data class Id(
        val ownerId: String,
        val resultId: String
    )

    internal object ResultIdKey : NavigationKey.MetadataKey<Id?>(null)

    @PublishedApi
    internal companion object {
        @PublishedApi
        internal val pendingResults: MutableStateFlow<Map<Id, NavigationResult<*>>> = MutableStateFlow(emptyMap())

        @PublishedApi
        internal val activeChannels: MutableSet<Id> = mutableSetOf()

        @PublishedApi
        internal inline fun <reified T : Any> observe(
            scope: CoroutineScope,
            resultChannel: NavigationResultChannel<T>,
        ) : Job {
            return observe(T::class, scope, resultChannel)
        }

        @PublishedApi
        internal fun <T : Any> observe(
            resultType: KClass<T>,
            scope: CoroutineScope,
            resultChannel: NavigationResultChannel<T>,
        ) : Job {
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

// TODO this needs much more documentation, it's too complex, maybe a separate file
@Composable
public inline fun <reified R : Any> registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onCompleted: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
): NavigationResultChannel<R> {
    val hashKey = currentCompositeKeyHash
    val navigationHandle = LocalNavigationHandle.current
    val channel = remember(hashKey) {
        NavigationResultChannel<R>(
            id = NavigationResultChannel.Id(
                ownerId = navigationHandle.id,
                resultId = hashKey.toString(),
            ),
            navigationHandle = navigationHandle,
            onClosed = {
                @Suppress("UNCHECKED_CAST")
                this as NavigationResultScope<out NavigationKey.WithResult<out R>>
                onClosed(this)
            },
            onCompleted = {
                @Suppress("UNCHECKED_CAST")
                this as NavigationResultScope<out NavigationKey.WithResult<out R>>
                onCompleted(it)
            }
        )
    }
    LaunchedEffect(hashKey) {
        NavigationResultChannel.observe(this, channel)
    }
    return channel
}

// TODO this needs much more documentation, it's too complex, maybe a separate file
@Composable
public fun registerForNavigationResult(
    onClosed: NavigationResultScope<out NavigationKey>.() -> Unit = {},
    onCompleted: NavigationResultScope<out NavigationKey>.() -> Unit,
): NavigationResultChannel<Unit> {
    val hashKey = currentCompositeKeyHash
    val navigationHandle = LocalNavigationHandle.current
    val channel = remember(hashKey) {
        NavigationResultChannel<Unit>(
            id = NavigationResultChannel.Id(
                ownerId = navigationHandle.id,
                resultId = hashKey.toString(),
            ),
            navigationHandle = navigationHandle,
            onClosed = onClosed,
            onCompleted = {
                onCompleted()
            }
        )
    }
    LaunchedEffect(hashKey) {
        NavigationResultChannel.observe<Unit>(this, channel)
    }
    @Suppress("UNCHECKED_CAST")
    return channel
}

public inline fun <reified R : Any> ViewModel.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onCompleted: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
): ReadOnlyProperty<ViewModel, NavigationResultChannel<R>> {
    return registerForNavigationResult(
        resultType = R::class,
        onClosed = onClosed,
        onCompleted = onCompleted,
    )
}

public fun <R : Any> ViewModel.registerForNavigationResult(
    resultType: KClass<R>,
    onClosed: NavigationResultScope<NavigationKey.WithResult<R>>.() -> Unit = {},
    onCompleted: NavigationResultScope<NavigationKey.WithResult<R>>.(R) -> Unit,
): ReadOnlyProperty<ViewModel, NavigationResultChannel<R>> {
    val navigation = getNavigationHandle()
    val scope = viewModelScope
    @Suppress("UNCHECKED_CAST")
    val channel = NavigationResultChannel<R>(
        id = NavigationResultChannel.Id(
            ownerId = navigation.id,
            resultId = onClosed::class.qualifiedName + onCompleted::class.qualifiedName,
        ),
        onClosed = {
            this as NavigationResultScope<NavigationKey.WithResult<R>>
            onClosed()
        },
        onCompleted = {
            this as NavigationResultScope<NavigationKey.WithResult<R>>
            onCompleted(it)
        },
        navigationHandle = navigation,
    )
    NavigationResultChannel.observe(resultType, scope, channel)
    return ReadOnlyProperty { vm, _ ->
        require(vm === this)
        channel
    }
}

public fun ViewModel.registerForNavigationResult(
    onClosed: NavigationResultScope<out NavigationKey>.() -> Unit = {},
    onCompleted: NavigationResultScope<out NavigationKey>.() -> Unit,
): ReadOnlyProperty<ViewModel, NavigationResultChannel<Unit>> {
    val navigation = getNavigationHandle()
    val scope = viewModelScope
    @Suppress("UNCHECKED_CAST")
    val channel = NavigationResultChannel<Unit>(
        id = NavigationResultChannel.Id(
            ownerId = navigation.id,
            resultId = onClosed::class.qualifiedName + onCompleted::class.qualifiedName,
        ),
        onClosed = {
            onClosed()
        },
        onCompleted = {
            onCompleted()
        },
        navigationHandle = navigation,
    )
    NavigationResultChannel.observe(Unit::class, scope, channel)
    return ReadOnlyProperty { vm, _ ->
        require(vm === this)
        channel
    }
}