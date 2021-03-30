package dev.enro.core.compose

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.core.*
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import kotlinx.coroutines.delay

internal val LocalComposableContainer = compositionLocalOf<ComposableContainer> {
    throw IllegalStateException("The current composition does not have a ComposableContainer attached")
}

class ComposableContainer(
    internal val initialState: List<NavigationInstruction.Open>,
    internal val navigationController: () -> NavigationController,
    internal val hostContext: () -> NavigationContext<out Fragment>
) {

    val backstackState = MutableLiveData<List<NavigationInstruction.Open>>(initialState)
    private val destinations = mutableMapOf<String, ComposableDestination>()

    private val currentDestination get() = backstackState.value?.lastOrNull()?.instructionId?.let { destinations[it] }
    private val previousState = MutableLiveData<NavigationInstruction.Open?>()

    internal val context: NavigationContext<*>? get() = currentDestination?.getNavigationHandleViewModel()?.navigationContext

    fun push(instruction: NavigationInstruction.Open) {
        when (instruction.navigationDirection) {
            NavigationDirection.FORWARD -> {
                backstackState.value = backstackState.value.orEmpty().plus(instruction)
            }
            NavigationDirection.REPLACE -> {
                previousState.value = backstackState.value?.lastOrNull()
                backstackState.value = backstackState.value.orEmpty().dropLast(1).plus(instruction)
            }
            NavigationDirection.REPLACE_ROOT -> {
                previousState.value = backstackState.value?.lastOrNull()
                backstackState.value = listOf(instruction)
            }
        }
    }

    fun close() {
        backstackState.value?.lastOrNull()?.let {
            val destination = destinations[it.instructionId] ?: return@let
            navigationController().onComposeDestinationClosed(destination)
        }

        if (backstackState.value?.size == 1) {
            context?.controller?.close(hostContext())
            return
        }

        previousState.value = backstackState.value?.lastOrNull()
        backstackState.value = backstackState.value?.dropLast(1)
    }

    @Composable
    fun Render() {
        val backstack = backstackState.observeAsState()
        val previous = previousState.observeAsState()
        val visible = backstack.value?.lastOrNull() ?: return
        val context = LocalContext.current as FragmentActivity

        val animations = remember(visible) {
            if (visible == null) return@remember AnimationPair.Resource(0, 0)
            animationsFor(
                    context.navigationContext,
                    if (previous.value == null) visible else NavigationInstruction.Close
            )
        }

        CompositionLocalProvider(
                LocalComposableContainer provides this
        ) {
            var toRender = backstack.value.orEmpty()
            if (previous.value != null) {
                toRender = toRender + previous.value!!
            }
            // TODO - sometimes renders too much (i.e. renders the item behind the current item)
            toRender.takeLast(2).forEach {
                key(it.instructionId) {
                    val animationState = getAnimationResourceState(if (visible == it) animations.enter else animations.exit)
                    if (animationState.isActive || visible == it) {
                        Box(
                                modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                                alpha = animationState.alpha,
                                                scaleX = animationState.scaleX,
                                                scaleY = animationState.scaleY,
                                                rotationX = animationState.rotationX,
                                                rotationY = animationState.rotationY,
                                                translationX = animationState.translationX,
                                                translationY = animationState.translationY,
                                        )
                                        .pointerInteropFilter { _ ->
                                            it != visible
                                        }
                        ) {
                            getDestination(it).InternalRender()
                        }
                    }
                    if (!animationState.isActive && it == previous.value) {
                        previousState.value = null
                    }
                }
            }
        }

        remember(visible) {
            if (visible == null) {
                true
            } else {
                val destination = destinations[visible.instructionId] ?: return@remember true
                navigationController().onComposeDestinationActive(destination)
                true
            }
        }
    }

    @Composable
    private fun getDestination(instruction: NavigationInstruction.Open): ComposableDestination {
        return destinations.getOrPut(instruction.instructionId) {
            val controller = navigationController()
            val composeKey = instruction.navigationKey
            val destination = controller.navigatorForKeyType(composeKey::class)!!.contextType.java
                .newInstance() as ComposableDestination
            destination.instruction = instruction

            destination.activity = LocalContext.current as FragmentActivity
            destination.container = LocalComposableContainer.current
            destination.lifecycleOwner = LocalLifecycleOwner.current
            destination.viewModelStoreOwner = viewModel<ComposableDestinationViewModelStoreOwner>(instruction.instructionId)

            controller.onComposeDestinationAttached(destination)

            return@getOrPut destination
        }
    }
}

internal class AnimatorView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }
}

internal data class AnimationResourceState(
        val alpha: Float = 1.0f,
        val scaleX: Float = 1.0f,
        val scaleY: Float = 1.0f,
        val translationX: Float = 0.0f,
        val translationY: Float = 0.0f,
        val rotationX: Float = 0.0f,
        val rotationY: Float = 0.0f,

        val playTime: Long = 0,
        val isActive: Boolean = false
)

@Composable
internal fun getAnimationResourceState(animOrAnimator: Int): AnimationResourceState {
    val state = remember(animOrAnimator) { mutableStateOf(AnimationResourceState()) }
    if (animOrAnimator == 0) return state.value

    updateAnimationResourceStateFromAnim(state, animOrAnimator)
    updateAnimationResourceStateFromAnimator(state, animOrAnimator)

    LaunchedEffect(animOrAnimator) {
        val start = System.currentTimeMillis()
        while (state.value.isActive) {
            state.value = state.value.copy(playTime = System.currentTimeMillis() - start)
            delay(8)
        }
    }
    return state.value
}

@Composable
private fun updateAnimationResourceStateFromAnim(state: MutableState<AnimationResourceState>, animOrAnimator: Int) {
    val context = LocalContext.current
    val isAnim = remember(animOrAnimator) { context.resources.getResourceTypeName(animOrAnimator) == "anim" }
    if (!isAnim) return

    val anim = remember(animOrAnimator) { AnimationUtils.loadAnimation(context, animOrAnimator) }

    val transformation = Transformation()
    anim.getTransformation(System.currentTimeMillis(), transformation)

    val v = FloatArray(9)
    transformation.matrix.getValues(v)
    state.value = AnimationResourceState(
            alpha = transformation.alpha,
            scaleX = v[android.graphics.Matrix.MSCALE_X],
            scaleY = v[android.graphics.Matrix.MSCALE_Y],
            translationX = v[android.graphics.Matrix.MTRANS_X],
            translationY = v[android.graphics.Matrix.MTRANS_Y],
            rotationX = 0.0f,
            rotationY = 0.0f,

            playTime = state.value.playTime,
            isActive = state.value.playTime < anim.duration,
    )
}

@Composable
private fun updateAnimationResourceStateFromAnimator(state: MutableState<AnimationResourceState>, animOrAnimator: Int) {
    val context = LocalContext.current
    val isAnimator = remember(animOrAnimator) { context.resources.getResourceTypeName(animOrAnimator) == "animator" }
    if (!isAnimator) return

    val animator = remember(animOrAnimator) {
        state.value = AnimationResourceState(
                alpha = 0.0f,
                isActive = true
        )
        AnimatorInflater.loadAnimator(context, animOrAnimator)
    }

    AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                AnimatorView(it).apply {
                    animator.setTarget(this)
                    animator.start()
                }
            },
            update = {
                state.value = AnimationResourceState(
                        alpha = it.alpha,
                        scaleX = it.scaleX,
                        scaleY = it.scaleY,
                        translationX = it.translationX,
                        translationY = it.translationY,
                        rotationX = it.rotationX,
                        rotationY = it.rotationY,

                        isActive = animator.isRunning,
                        playTime = state.value.playTime
                )
            }
    )
}