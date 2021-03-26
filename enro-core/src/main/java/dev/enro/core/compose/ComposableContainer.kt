package dev.enro.core.compose

import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import dev.enro.core.*
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import java.lang.Math.max

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
        when (destination.instruction.navigationDirection) {
            NavigationDirection.FORWARD -> {
                backstackState.value =
                    backstackState.value?.plus(destination to index.value!!)
            }
            NavigationDirection.REPLACE -> {
                previousState.value = backstackState.value?.lastOrNull()
                backstackState.value =
                    backstackState.value?.dropLast(1)?.plus(destination to index.value!!)
            }
            NavigationDirection.REPLACE_ROOT -> {
                previousState.value = backstackState.value?.lastOrNull()
                backstackState.value =
                    listOf(destination to index.value!!)
            }
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
        val context = LocalContext.current as FragmentActivity

        val (enter, exit) = remember(visible) {
            if(visible == null) return@remember Pair( AnimationSet(false), AnimationSet(false))

            val animations = animationsFor(
                context.navigationContext,
                if(previous.value == null) visible.first.instruction else NavigationInstruction.Close
            )
            val enter = AnimationUtils.loadAnimation(context, animations.enter)
            val exit =  AnimationUtils.loadAnimation(context, animations.exit)

            return@remember Pair(enter, exit)
        }

        CompositionLocalProvider(
            LocalComposableContainer provides this
        ) {
            var toRender = backstack.value
            if(previous.value != null) {
                toRender = toRender + previous.value!!
            }
            toRender.forEach {
                key(it.second) {
                        val targetAlpha = remember {
                            mutableStateOf(0.0f)
                        }
                        SideEffect {
                            targetAlpha.value = if (it == visible) 1.0f else 0.0f
                        }
                        val currentTimeAnimated by animateFloatAsState(
                            targetValue = targetAlpha.value,
                            animationSpec = tween(
                                durationMillis = max(enter.computeDurationHint().toInt(), exit.computeDurationHint().toInt()),
                            ),
                            finishedListener = { _ ->
                                if (it == previous.value) {
                                    previousState.value = null
                                }
                            }
                        )

                        val transformation = Transformation()
                        var shouldShow = true
                        if (it == visible && !currentTimeAnimated.isNaN()) {
                            enter.getTransformation(System.currentTimeMillis(), transformation)
                        } else {
                            val isRunning =
                                exit.getTransformation(System.currentTimeMillis(), transformation)
                            shouldShow = isRunning
                        }

                        val v = FloatArray(9)
                        transformation.matrix.getValues(v)
                        val scaleX = v[android.graphics.Matrix.MSCALE_X]
                        val skewX = v[android.graphics.Matrix.MSKEW_X]
                        val translateX = v[android.graphics.Matrix.MTRANS_X]
                        val skewY = v[android.graphics.Matrix.MSKEW_Y]
                        val scaleY = v[android.graphics.Matrix.MSCALE_Y]
                        val translateY = v[android.graphics.Matrix.MTRANS_Y]
                        val persp0 = v[android.graphics.Matrix.MPERSP_0]
                        val persp1 = v[android.graphics.Matrix.MPERSP_1]
                        val persp2 = v[android.graphics.Matrix.MPERSP_2]

                        if (currentTimeAnimated > 0.01 || !it.first.initialised) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = scaleX,
                                        scaleY = scaleY,
                                        alpha = transformation.alpha,
                                        translationX = translateX,
                                        translationY = translateY,
                                    )
                                    .pointerInteropFilter { _ ->
                                        it != visible
                                    }
                            ) {
                                it.first.InternalRender()
                            }
                        }
                }
            }
        }

        remember(visible) {
            if(visible == null) {
                true
            }
            else {
                navigationController().onComposeDestinationActive(visible.first)
                true
            }
        }
    }
}