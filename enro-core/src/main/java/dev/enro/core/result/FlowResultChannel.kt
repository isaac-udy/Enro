package dev.enro.core.result

import androidx.compose.runtime.mutableStateMapOf
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.core.*
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.result.internal.ResultChannelImpl
import dev.enro.viewmodel.getNavigationHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.parcelize.Parcelize
import kotlin.properties.ReadOnlyProperty

@PublishedApi
internal class FlowResultManager {
    val results = mutableStateMapOf<Int, CompletedFlowStep>()
}

@PublishedApi
internal data class FlowStep(
    val order: Int,
    val key: NavigationKey,
    val dependsOn: List<Any>,
    val onCompleted: suspend () -> Unit = {},
) {
    fun complete(result: Any): CompletedFlowStep {
        return CompletedFlowStep(
            order, key, result, dependsOn
        )
    }
}

@Parcelize
internal data class FlowStepKey(
    val order: Int,
) :
    NavigationKey.SupportsPush.WithResult<Any>,
    NavigationKey.SupportsPresent.WithResult<Any>


@PublishedApi
internal data class CompletedFlowStep(
    val order: Int,
    val key: NavigationKey,
    val result: Any,
    val dependsOn: List<Any>,
)

public class NavigationFlowScope internal constructor(
    @PublishedApi
    internal val resultManager: FlowResultManager
) {
    @PublishedApi
    internal val steps: MutableList<FlowStep> = mutableListOf()

    public inline fun <reified T : Any> push(
        key: NavigationKey.SupportsPush.WithResult<T>,
        dependsOn: List<Any> = emptyList(),
        noinline onCompleted: suspend () -> Unit = {},
    ): T {
        val step = FlowStep(
            order = steps.size,
            key = key,
            dependsOn = dependsOn,
            onCompleted = onCompleted,
        )
        steps.add(step)

        val completedStep = resultManager.results[step.order]?.let {
            if (it.result !is T) {
                resultManager.results.remove(it.order)
                return@let null
            }
            if (it.dependsOn != dependsOn) {
                resultManager.results.remove(it.order)
                return@let null
            }
            it
        }
        return completedStep?.result as? T ?: throw NoResultForPush(step)
    }

    public inline fun <reified T : Any> present(
        key: NavigationKey.SupportsPresent.WithResult<T>,
        dependsOn: List<Any> = emptyList(),
        noinline onCompleted: suspend () -> Unit = {},
    ): T {
        val step = FlowStep(
            order = steps.size,
            key = key,
            dependsOn = dependsOn,
            onCompleted = onCompleted,
        )
        steps.add(step)

        val completedStep = resultManager.results[step.order]?.let {
            if (it.result !is T) {
                resultManager.results.remove(it.order)
                return@let null
            }
            if (it.dependsOn != dependsOn) {
                resultManager.results.remove(it.order)
                return@let null
            }
            it
        }
        return completedStep?.result as? T ?: throw NoResultForPresent(step)
    }

    public fun escape(): Nothing {
        throw Escape()
    }

    @PublishedApi
    internal class NoResultForPush(val step: FlowStep) : RuntimeException()

    @PublishedApi
    internal class NoResultForPresent(val step: FlowStep) : RuntimeException()

    @PublishedApi
    internal class Escape : RuntimeException()
}


internal fun interface CreateResultChannel {
    operator fun invoke(
        onClosed: (Any) -> Unit,
        onResult: (NavigationKey.WithResult<*>, Any) -> Unit
    ): EnroResultChannel<Any, NavigationKey.WithResult<Any>>
}

@AdvancedEnroApi
public class NavigationFlow<T> internal constructor(
    private val scope: CoroutineScope,
    private val navigationHandle: NavigationHandle,
    private val registerForNavigationResult: CreateResultChannel,
    private val flow: NavigationFlowScope.() -> T,
    private val onCompleted: (T) -> Unit,
) {
    private var steps: List<FlowStep> = emptyList()
    private val resultManager = FlowResultManager()
    private val resultChannel = registerForNavigationResult(
        onClosed = { key ->
            if (key !is FlowStepKey) return@registerForNavigationResult
            resultManager.results.remove(key.order)
        },
        onResult = { key, result ->
            if (key !is FlowStepKey) return@registerForNavigationResult
            val step = steps.first { it.order == key.order }
            resultManager.results[key.order] = step.complete(result)
            next()
        },
    )

    public fun next() {
        val flowScope = NavigationFlowScope(resultManager)
//        scope.launch {
            runCatching { flowScope.flow() }
                .onSuccess {
                    steps = flowScope.steps.toList()
                    onCompleted(it)
                }
                .onFailure {
                    steps = flowScope.steps.toList()
                    val resultChannelId = (resultChannel as ResultChannelImpl<*,*>).id
                    when(it) {
                        is NavigationFlowScope.NoResultForPush -> {
                            val instruction = NavigationInstruction.Open.OpenInternal(
                                navigationDirection = NavigationDirection.Push,
                                navigationKey = it.step.key,
                                resultId = resultChannelId,
                                additionalData = bundleOf(
                                    IS_PUSHED_IN_FLOW to true,
                                    PendingResult.OVERRIDE_NAVIGATION_KEY_EXTRA to FlowStepKey(it.step.order)
                                )
                            )
                            navigationHandle.executeInstruction(instruction)
                        }
                        is NavigationFlowScope.NoResultForPresent -> {
                            val instruction = NavigationInstruction.Open.OpenInternal(
                                navigationDirection = NavigationDirection.Present,
                                navigationKey = it.step.key,
                                resultId = resultChannelId,
                                additionalData = bundleOf(
                                    PendingResult.OVERRIDE_NAVIGATION_KEY_EXTRA to FlowStepKey(it.step.order)
                                )
                            )
                            navigationHandle.executeInstruction(instruction)
                        }
                        is NavigationFlowScope.Escape -> {}
                        else -> throw it
                    }
                }
//        }
    }

    internal companion object {
        const val IS_PUSHED_IN_FLOW = "NavigationFlow.IS_PUSHED_IN_FLOW"
    }
}

public fun <T> ViewModel.registerForFlowResult(
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): ReadOnlyProperty<ViewModel, NavigationFlow<T>> {
    return ReadOnlyProperty { thisRef, property ->
        NavigationFlow(
            scope = viewModelScope,
            navigationHandle = getNavigationHandle(),
            registerForNavigationResult = { onClosed, onResult ->
                registerForNavigationResultWithKey(
                    onClosed = onClosed,
                    onResult = onResult,
                ).getValue(thisRef, property)
            },
            flow = flow,
            onCompleted = onCompleted,
        )
    }
}