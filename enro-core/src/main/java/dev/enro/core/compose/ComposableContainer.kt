package dev.enro.core.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.activity
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.core.navigationContext

internal val LocalComposableContainer = compositionLocalOf<ComposableContainer> {
    throw IllegalStateException("The current composition does not have a ComposableContainer attached")
}

class ComposableContainer(
    private val navigationController: () -> NavigationController
) {

    private val backstackState =
        MutableLiveData<List<Pair<ComposableDestination, Int>>>(emptyList())

    private val previousState =
        MutableLiveData<Pair<ComposableDestination, Int>?>() // TODO Saved State

    private val index = MutableLiveData(0)

    internal val context: NavigationContext<*>? get() = backstackState.value?.lastOrNull()?.first?.getNavigationHandleViewModel()?.navigationContext

    fun push(destination: ComposableDestination) {
        index.value = index.value!! + 1
        previousState.value = backstackState.value?.lastOrNull()
        when (destination.instruction.navigationDirection) {
            NavigationDirection.FORWARD -> backstackState.value =
                backstackState.value?.plus(destination to index.value!!)
            NavigationDirection.REPLACE -> backstackState.value =
                backstackState.value?.dropLast(1)?.plus(destination to index.value!!)
            NavigationDirection.REPLACE_ROOT -> backstackState.value =
                listOf(destination to index.value!!)
        }
    }

    fun close() {
        backstackState.value?.lastOrNull()?.let {
            // tell navigation controller we opened something
            navigationController().onComposeDestinationClosed(it.first)
        }

        if (backstackState.value?.size == 1) {
            context?.controller?.close(context?.activity?.navigationContext ?: return)
        }

        previousState.value = backstackState.value?.lastOrNull()
        backstackState.value = backstackState.value?.dropLast(1)
    }

    @Composable
    fun Render() {
        val backstack = backstackState.observeAsState(initial = emptyList())
        val previous = previousState.observeAsState()
        val visible = backstack.value.lastOrNull()

        CompositionLocalProvider(
            LocalComposableContainer provides this
        ) {
            (backstack.value + previous.value).forEach {
                if (it == null) return@forEach
                key(it.second) {
                    val targetAlpha = remember {
                        mutableStateOf(0.0f)
                    }
                    SideEffect {
                        targetAlpha.value = if (it == visible) 1.0f else 0.0f
                    }
                    val alpha by animateFloatAsState(
                        targetValue = targetAlpha.value,
                        animationSpec = tween(
                            durationMillis = 225,
                            delayMillis = ((1.0f - targetAlpha.value) * 125).toInt()
                        ),
                        finishedListener = { _ ->
                            if (it == previous.value) {
                                previousState.value = null
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .alpha(alpha)
                            .offset(y = (40 - (40 * alpha)).dp)
                    ) {
                        it.first.InternalRender()
                    }

                    remember(it == visible) {
                        if (it == visible) {
                            navigationController().onComposeDestinationActive(it.first)
                        }
                        true
                    }

                }
            }
        }
    }
}