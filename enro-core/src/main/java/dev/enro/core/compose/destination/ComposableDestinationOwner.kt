package dev.enro.core.compose.destination

import android.annotation.SuppressLint
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeOut
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.dialog.*
import dev.enro.core.container.NavigationContainer
import dev.enro.core.controller.usecase.ComposeEnvironment
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved

@Stable
internal class ComposableDestinationOwner(
    parentContainer: NavigationContainer,
    val instruction: AnyOpenInstruction,
    val destination: ComposableDestination,
    onNavigationContextCreated: OnNavigationContextCreated,
    onNavigationContextSaved: OnNavigationContextSaved,
    private val composeEnvironment: ComposeEnvironment,
    viewModelStore: ViewModelStore,
) : ViewModel(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {

    internal val transitionState = MutableTransitionState(false)
    internal var _parentContainer: NavigationContainer? = parentContainer
    internal val parentContainer get() = _parentContainer!!

    @SuppressLint("StaticFieldLeak")
    @Suppress("LeakingThis")
    private val lifecycleRegistry = LifecycleRegistry(this)

    @Suppress("LeakingThis")
    private val savedStateRegistryOwner = ComposableDestinationSavedStateRegistryOwner(this, onNavigationContextSaved)

    @Suppress("LeakingThis")
    private val viewModelStoreOwner = ComposableDestinationViewModelStoreOwner(
        owner = this,
        savedState = savedStateRegistryOwner.savedState,
        viewModelStore = viewModelStore
    )

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryOwner.savedStateRegistry

    init {
        destination.owner = this
        onNavigationContextCreated(
            destination.context,
            savedStateRegistryOwner.savedState
        )
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override val lifecycle: Lifecycle get() {
        return lifecycleRegistry
    }

    override val viewModelStore: ViewModelStore get() {
        return viewModelStoreOwner.viewModelStore
    }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory get() {
        return viewModelStoreOwner.defaultViewModelProviderFactory
    }

    override val defaultViewModelCreationExtras: CreationExtras get() {
        return viewModelStoreOwner.defaultViewModelCreationExtras
    }

    internal fun destroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _parentContainer = null
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    internal fun Render(backstackState: List<AnyOpenInstruction>) {
        if (_parentContainer == null) return
        val lifecycleState = rememberLifecycleState()
        if (!lifecycleState.isAtLeast(Lifecycle.State.CREATED)) return

        val saveableStateHolder = rememberSaveableStateHolder()

        val renderDestination = remember(instruction.instructionId) {
            movableContentOf {
                ProvideCompositionLocals {
                    saveableStateHolder.SaveableStateProvider(key = instruction.instructionId) {
                        destination.Render()
                    }
                }
            }
        }
        val animation = remember(transitionState.targetState) {
            when (destination) {
                is DialogDestination -> NavigationAnimation.Composable(
                        forView = DefaultAnimations.ForView.none,
                        enter = EnterTransition.None,
                        exit = ExitTransition.None,
                )
                is BottomSheetDestination -> NavigationAnimation.Composable(
                    forView = DefaultAnimations.ForView.none,
                    enter = EnterTransition.None,
                    exit = fadeOut(tween(150, 350)),
                )
                else -> parentContainer.currentAnimations.asComposable()
            }
        }
        val transition = updateTransition(transitionState, "ComposableDestination Visibility")
        ReusableContent(instruction.instructionId) {
            if (!lifecycleState.isAtLeast(Lifecycle.State.STARTED)
                && !transition.targetState
                && destination is DialogDestination
            ) return@ReusableContent

            ProvideRenderingWindow {
                animation.content(transition) {
                    renderDestination()
                    RegisterComposableLifecycleState(backstackState)
                }
            }
        }
    }

    @Composable
    private fun RegisterComposableLifecycleState(
        backstackState: List<AnyOpenInstruction>,
    ) {
        val parentLifecycle = parentContainer.parentContext.lifecycleOwner.rememberLifecycleState()
        DisposableEffect(backstackState, parentLifecycle) {
            val isActive = backstackState.lastOrNull() == instruction
            val isInBackstack = backstackState.contains(instruction)
            val targetLifecycle = when {
                isActive -> Lifecycle.State.RESUMED
                isInBackstack -> Lifecycle.State.STARTED
                else -> lifecycle.currentState
            }

            lifecycleRegistry.currentState = minOf(parentLifecycle, targetLifecycle)

            onDispose {
                when {
                    isActive -> {}
                    isInBackstack -> lifecycleRegistry.currentState = Lifecycle.State.CREATED
                    else -> {
                        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                        _parentContainer = null
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun ProvideRenderingWindow(
        content: @Composable () -> Unit
    ) {
        when {
            destination is DialogDestination -> EnroDialogContainer(
                navigationHandle = destination.getNavigationHandle(),
                destination = destination,
                content = content
            )
            destination is BottomSheetDestination -> EnroBottomSheetContainer(
                navigationHandle = destination.getNavigationHandle(),
                destination = destination,
                content = content
            )
            instruction.navigationDirection is NavigationDirection.Present -> {
                Dialog(
                    onDismissRequest = { getNavigationHandle().requestClose() },
                    properties = DialogProperties(
                        dismissOnClickOutside = false,
                        usePlatformDefaultWidth = false,
                    )
                ) {
                    requireNotNull(rememberDialogWindowProvider())
                        .window
                        .apply {
                            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                            setWindowAnimations(0)
                        }
                    content()
                }
            }
            else -> content()
        }
    }

    @Composable
    private fun ProvideCompositionLocals(
        content: @Composable () -> Unit,
    ) {
        CompositionLocalProvider(
            LocalLifecycleOwner provides this@ComposableDestinationOwner,
            LocalViewModelStoreOwner provides this@ComposableDestinationOwner,
            LocalSavedStateRegistryOwner provides this@ComposableDestinationOwner,
            LocalNavigationHandle provides remember { getNavigationHandle() }
        ) {
            composeEnvironment {
                content()
            }
        }
    }
}

internal val ComposableDestinationOwner.navigationController get() = parentContainer.parentContext.controller
internal val ComposableDestinationOwner.parentSavedStateRegistry get() = parentContainer.parentContext.savedStateRegistryOwner.savedStateRegistry
internal val ComposableDestinationOwner.activity: ComponentActivity get() = parentContainer.parentContext.activity

@Composable
internal fun LifecycleOwner.rememberLifecycleState() : Lifecycle.State {
    val activeState = remember(this, lifecycle.currentState) { mutableStateOf(lifecycle.currentState) }

    DisposableEffect(this, activeState) {
        val observer = LifecycleEventObserver { _, event ->
            activeState.value = event.targetState
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return activeState.value
}