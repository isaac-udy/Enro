package dev.enro.core.compose.destination

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReusableContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.animation.NavigationAnimation
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.activity
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.dialog.EnroBottomSheetContainer
import dev.enro.core.compose.dialog.EnroDialogContainer
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.getAnimationsForEntering
import dev.enro.core.container.getAnimationsForExiting
import dev.enro.core.controller.usecase.ComposeEnvironment
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.getNavigationHandle
import dev.enro.extensions.rememberLifecycleState
import java.lang.ref.WeakReference

internal class ComposableDestinationOwner(
    parentContainer: NavigationContainer,
    val instruction: AnyOpenInstruction,
    val destination: ComposableDestination,
    onNavigationContextCreated: OnNavigationContextCreated,
    private val onNavigationContextSaved: OnNavigationContextSaved,
    private val composeEnvironment: ComposeEnvironment,
    viewModelStore: ViewModelStore,
    private val savedInstanceState: Bundle?,
) : ViewModel(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {

    internal var transitionState by mutableStateOf(MutableTransitionState(false))
    internal lateinit var transition: Transition<Boolean>
    internal var animationOverride by mutableStateOf<NavigationAnimation.Composable?>(null)
    private var _parentContainer: NavigationContainer? = parentContainer
    private var weakParentContainerReference: WeakReference<NavigationContainer> = WeakReference(parentContainer)
    internal val parentContainer get() = weakParentContainerReference.get() ?: _parentContainer!!

    @SuppressLint("StaticFieldLeak")
    @Suppress("LeakingThis")
    private val lifecycleRegistry = LifecycleRegistry(this)

    @Suppress("LeakingThis")
    private val savedStateRegistryOwner = ComposableDestinationSavedStateRegistryOwner(this, savedInstanceState)

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
            context = destination.context,
            savedInstanceState = savedInstanceState
        )
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    internal fun save(): Bundle {
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) return Bundle()
        return Bundle().also {
            savedStateRegistry.performSave(it)
            onNavigationContextSaved(
                context = destination.context,
                outState = it
            )
        }
    }

    override val lifecycle: Lifecycle
        get() {
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

        val parentContainer = _parentContainer
        when {
            parentContainer == null -> viewModelStore.clear()
            !parentContainer.backstack.contains(instruction) -> viewModelStore.clear()
        }
        _parentContainer = null
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    internal fun Render(
        backstackState: List<AnyOpenInstruction>,
    ) {
        val parentContainer = _parentContainer ?: return
        val lifecycleState = rememberLifecycleState()
        if (!lifecycleState.isAtLeast(Lifecycle.State.CREATED)) return

        val renderDestination = remember(instruction.instructionId) {
            movableContentOf {
                ProvideCompositionLocals {
                    savedStateRegistryOwner.SaveableStateProvider {
                        destination.Render()
                    }
                }
            }
        }

        val animation = remember(instruction.instructionId, transitionState.targetState, parentContainer, animationOverride) {
            animationOverride ?: when (destination) {
                is DialogDestination -> NavigationAnimation.Composable(EnterTransition.None, ExitTransition.None)
                is BottomSheetDestination -> NavigationAnimation.Composable(
                    enter = EnterTransition.None,
                    exit = fadeOut(tween(75, 150)),
                )
                else -> when (transitionState.targetState) {
                    true -> parentContainer.getAnimationsForEntering(instruction).asComposable()
                    else -> parentContainer.getAnimationsForExiting(instruction).asComposable()
                }
            }
        }
        transition = updateTransition(transitionState, "ComposableDestination Visibility")

        if (!lifecycleState.isAtLeast(Lifecycle.State.STARTED)
            && !transition.currentState
            && !transition.targetState
        ) return

        ReusableContent(instruction.instructionId) {
            ProvideRenderingWindow(backstackState) {
                animation.Animate(transition) {
                    renderDestination()
                    RegisterComposableLifecycleState(backstackState, parentContainer)
                }
            }
        }
    }

    @Composable
    private fun RegisterComposableLifecycleState(
        backstackState: List<AnyOpenInstruction>,
        parentContainer: NavigationContainer,
    ) {
        val parentLifecycle = parentContainer.context.lifecycleOwner.rememberLifecycleState()
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
                    else -> destroy()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun ProvideRenderingWindow(
        backstackState: List<AnyOpenInstruction>,
        content: @Composable () -> Unit,
    ) {
        val isActive = remember(backstackState) {
            backstackState.lastOrNull() == instruction
        }
        Box(
            // When this ComposableDestinationOwner is not the current active item in the backstack, we're going to
            // hide it from accessibility, so that you can't focus "through" presented destinations
            modifier = Modifier.run {
                when {
                    !isActive -> Modifier.clearAndSetSemantics { invisibleToUser() }
                    else -> this
                }
            }
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

                else -> content()
            }
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

internal val ComposableDestinationOwner.navigationController get() = parentContainer.context.controller
internal val ComposableDestinationOwner.parentSavedStateRegistry get() = parentContainer.context.savedStateRegistryOwner.savedStateRegistry
internal val ComposableDestinationOwner.activity: ComponentActivity get() = parentContainer.context.activity
