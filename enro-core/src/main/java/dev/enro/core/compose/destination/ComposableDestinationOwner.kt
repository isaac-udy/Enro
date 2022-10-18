package dev.enro.core.compose.destination

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.MutableTransitionState
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
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.activity
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ComposableDestinationOwner(
    parentContainer: NavigationContainer,
    val instruction: AnyOpenInstruction,
    val destination: ComposableDestination
) : ViewModel(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {

    private val parentContainerState = mutableStateOf(parentContainer)
    internal var parentContainer: NavigationContainer
        get() {
            return parentContainerState.value
        }
        set(value) {
            parentContainerState.value = value
        }


    internal val transitionState = MutableTransitionState(false)

    @SuppressLint("StaticFieldLeak")
    @Suppress("LeakingThis")
    private val lifecycleRegistry = LifecycleRegistry(this)

    @Suppress("LeakingThis")
    private val savedStateRegistryOwner = ComposableDestinationSavedStateRegistryOwner(this)

    @Suppress("LeakingThis")
    private val viewModelStoreOwner = ComposableDestinationViewModelStoreOwner(this, savedStateRegistryOwner.savedState)

    private val lifecycleFlow = createLifecycleFlow()

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryOwner.savedStateRegistry

    init {
        destination.owner = this
        navigationController.onComposeDestinationAttached(
            destination,
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

    internal fun clear() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    @Composable
    internal fun Render(backstackState: NavigationBackstack) {
        val lifecycleState by lifecycleFlow.collectAsState()
        if (!lifecycleState.isAtLeast(Lifecycle.State.CREATED)) return

        val saveableStateHolder = rememberSaveableStateHolder()
        if (
            transitionState.currentState == transitionState.targetState
            && !transitionState.currentState
            && instruction != backstackState.active
        ) return

        val renderDestination = remember {
            movableContentOf {
                ProvideRenderingEnvironment(saveableStateHolder) {
                    destination.Render()
                }
            }
        }

        val animation = remember(transitionState.targetState) { parentContainer.currentAnimations.asComposable() }
        animation.content(transitionState) {
            renderDestination()
            RegisterComposableLifecycleState(backstackState)
        }
    }

    @Composable
    private fun RegisterComposableLifecycleState(
        backstackState: NavigationBackstack
    ) {
        DisposableEffect(transitionState.currentState) {
            val isActive = transitionState.currentState
            val isStarted = lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)
            when {
                isActive -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                isStarted -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }

            onDispose {
                val isDestroyed = !backstackState.backstack.contains(instruction)
                when {
                    isDestroyed -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    isActive ->  lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                    else ->  lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                }
            }
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
            LocalNavigationHandle provides remember { getNavigationHandleViewModel() }
        ) {
            saveableStateHolder.SaveableStateProvider(key = instruction.instructionId) {
                navigationController.composeEnvironmentRepository.Render {
                    content()
                }
            }
        }
    }
}

internal val ComposableDestinationOwner.navigationController get() = parentContainer.parentContext.controller
internal val ComposableDestinationOwner.parentSavedStateRegistry get() = parentContainer.parentContext.savedStateRegistryOwner.savedStateRegistry
internal val ComposableDestinationOwner.activity: ComponentActivity get() = parentContainer.parentContext.activity

private fun LifecycleOwner.createLifecycleFlow(): StateFlow<Lifecycle.State> {
    val lifecycleFlow = MutableStateFlow(Lifecycle.State.INITIALIZED)
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            lifecycleFlow.value = source.lifecycle.currentState
        }
    })
    return lifecycleFlow
}