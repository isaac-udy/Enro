package dev.enro.core.compose.destination

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.updateTransition
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.dialog.EnroBottomSheetContainer
import dev.enro.core.compose.dialog.EnroDialogContainer
import dev.enro.core.container.NavigationContainer
import dev.enro.core.controller.usecase.ComposeEnvironment
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved

@Stable
internal class ComposableDestinationOwner(
    val parentContainer: NavigationContainer,
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

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStoreOwner.viewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return viewModelStoreOwner.defaultViewModelProviderFactory
    }

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        return viewModelStoreOwner.defaultViewModelCreationExtras
    }

    internal fun destroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    internal fun Render(backstackState: List<AnyOpenInstruction>) {
        val lifecycleState = rememberLifecycleState()
        val parentLifecycleState = parentContainer.parentContext.lifecycleOwner.rememberLifecycleState()
        if (!parentLifecycleState.isAtLeast(Lifecycle.State.CREATED)) return
        if (!lifecycleState.isAtLeast(Lifecycle.State.CREATED)) return

        val saveableStateHolder = rememberSaveableStateHolder()

//        /**
//         * This if statement does some important work, and unfortunately any proof work that it does is not able to be captured
//         * by an automated test at this stage; all attempts to recreate the error that occurs in the absence of this statement have
//         * failed to fail.
//         *
//         * To manually recreate the failure that occurs without this statement:
//         * Create a Composable destination ("CD") which can be presented and pushed
//         * Create a *Composable* transition between destinations of CD and CD
//         * ReplaceRoot with CD
//         * Push several other CD's (CD1, CD2, CD3, etc)
//         * Push a fragment destination (FD)
//         * Navigate back from FD to the top CD
//         * Navigating back from CD(n) to CD(n-1) should cause a crash due to movableContent IndexOutOfBounds errors occuring within
//         * the Compose internals
//         */
//        if (
//            transitionState.currentState == transitionState.targetState
//            && !transitionState.targetState
//            && instruction != backstackState.active
//            && instruction.navigationDirection == NavigationDirection.Push
//        ) return

        RegisterComposableLifecycleState(backstackState)
        val renderDestination = remember(instruction.instructionId) {
            movableContentOf {
                ProvideRenderingEnvironment(saveableStateHolder) {
                    when (destination) {
                        is DialogDestination -> EnroDialogContainer(destination, destination)
                        is BottomSheetDestination -> EnroBottomSheetContainer(destination, destination)
                        else -> destination.Render()
                    }
                }
            }
        }

        val animation = remember(transitionState.targetState) {
            when (destination) {
                is DialogDestination,
                is BottomSheetDestination -> {
                    NavigationAnimation.Composable(
                        forView = DefaultAnimations.ForView.none,
                        enter = EnterTransition.None,
                        exit = ExitTransition.None,
                    )
                }
                else -> parentContainer.currentAnimations.asComposable()
            }
        }
        val transition = updateTransition(transitionState, "ComposableDestination Visibility")
        animation.content(transition) {
            renderDestination()
        }
    }

    @Composable
    private fun RegisterComposableLifecycleState(
        backstackState: List<AnyOpenInstruction>
    ) {
        DisposableEffect(backstackState) {
            val isActive = backstackState.lastOrNull() == instruction
            val isStopped = backstackState.contains(instruction)
            when {
                isActive -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                isStopped -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                else -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }

            onDispose { }
        }
    }

    @Composable
    private fun ProvideRenderingEnvironment(
        saveableStateHolder: SaveableStateHolder,
        content: @Composable () -> Unit,
    ) {
        CompositionLocalProvider(
            LocalLifecycleOwner provides this@ComposableDestinationOwner,
            LocalViewModelStoreOwner provides this@ComposableDestinationOwner,
            LocalSavedStateRegistryOwner provides this@ComposableDestinationOwner,
            LocalNavigationHandle provides remember { getNavigationHandle() }
        ) {
            saveableStateHolder.SaveableStateProvider(key = instruction.instructionId) {
                composeEnvironment {
                    content()
                }
            }
        }
    }
}

internal val ComposableDestinationOwner.navigationController get() = parentContainer.parentContext.controller
internal val ComposableDestinationOwner.parentSavedStateRegistry get() = parentContainer.parentContext.savedStateRegistryOwner.savedStateRegistry
internal val ComposableDestinationOwner.activity: ComponentActivity get() = parentContainer.parentContext.activity

@Composable
internal fun LifecycleOwner.rememberLifecycleState() : Lifecycle.State {
    val activeState = remember(this) { mutableStateOf(lifecycle.currentState) }

    DisposableEffect(this) {
        val observer = LifecycleEventObserver { source, event ->
            activeState.value = event.targetState
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return lifecycle.currentState
//    return remember(activeState.value, lifecycle.currentState) {
//        Log.e("Lifecycle", "$this ${lifecycle.currentState} ${activeState.value.coerceAtMost(lifecycle.currentState)}")
//        activeState.value.coerceAtMost(lifecycle.currentState)
//    }
}