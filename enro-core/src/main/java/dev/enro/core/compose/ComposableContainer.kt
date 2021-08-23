package dev.enro.core.compose

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.core.*
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import kotlinx.coroutines.*

internal val LocalEnroContainerState = compositionLocalOf<EnroContainerState> {
    throw IllegalStateException("The current composition does not have a ComposableContainer attached")
}

@Composable
fun rememberEnroContainerState(
    accept: (NavigationKey) -> Boolean = { true }
): EnroContainerState {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current!!
    return remember {
        EnroContainerState(
            initialState = listOf(),
            navigationHandle = viewModelStoreOwner.getNavigationHandleViewModel(),
            accept = accept
        )
    }
}

data class EnroContainerState(
    private val initialState: List<NavigationInstruction.Open> = emptyList(),
    val navigationHandle: NavigationHandle,
    val accept: (NavigationKey) -> Boolean = { true }
) {
    private val backstackState = MutableLiveData(initialState)
    val backstack get() = backstackState.value.orEmpty()

    internal val navigationController: NavigationController = navigationHandle.controller
    internal val navigationContext: NavigationContext<*> =
        (navigationHandle as NavigationHandleViewModel).navigationContext!!

    internal val destinations = mutableMapOf<String, ComposableDestination>()
    internal val currentDestination get() = backstackState.value?.lastOrNull()?.instructionId?.let { destinations[it] }
    internal val previousState = MutableLiveData<NavigationInstruction.Open?>()

    internal val activeContext: NavigationContext<*>? get() = currentDestination?.getNavigationHandleViewModel()?.navigationContext

    fun push(instruction: NavigationInstruction.Open) {
        navigationContext.childComposableManager.primaryContainer = this
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
        val destination = currentDestination ?: return
        navigationController.onComposeDestinationClosed(destination)

        previousState.value = backstackState.value?.lastOrNull()
        backstackState.value = backstackState.value?.dropLast(1)

        if (backstackState.value.orEmpty().isEmpty()) {
            destination.parentContext?.childComposableManager?.primaryContainer = null
        }
    }

    @Composable
    fun observeBackstackAsState(): List<NavigationInstruction.Open> {
        val backstack = backstackState.observeAsState()
        val savedBackStack = rememberSaveable(backstack.value) {
            ArrayList(backstack.value.orEmpty())
        }

        if (backstack.value != savedBackStack) {
            backstackState.value = savedBackStack
            return savedBackStack
        }
        return backstack.value.orEmpty()
    }

    @Composable
    fun getDestination(instruction: NavigationInstruction.Open): ComposableDestination {
        val isNewDestination = !destinations.containsKey(instruction.instructionId)
        val destination = destinations.getOrPut(instruction.instructionId) {
            val controller = navigationController
            val composeKey = instruction.navigationKey
            val destination = controller.navigatorForKeyType(composeKey::class)!!.contextType.java
                .newInstance() as ComposableDestination
            destination.instruction = instruction

            destination.activity = LocalContext.current as FragmentActivity
            destination.containerState = LocalEnroContainerState.current
            destination.lifecycleOwner = LocalLifecycleOwner.current
            destination.viewModelStoreOwner =
                viewModel<ComposableDestinationViewModelStoreOwner>(
                    LocalViewModelStoreOwner.current!!,
                    instruction.instructionId
                )
            destination.parentContext = navigationContext
            return@getOrPut destination
        }

        val isRestoredKey = "dev.enro.core.compose.IS_RESTORED"
        val savedInstanceState = rememberSaveable(
            saver = object : Saver<Bundle, Bundle> {
                override fun restore(value: Bundle): Bundle {
                    value.putBoolean(isRestoredKey, true)
                    return value
                }

                override fun SaverScope.save(value: Bundle): Bundle {
                    navigationController.onComposeContextSaved(destination, value)
                    return value
                }
            }
        ) {
            Bundle().addOpenInstruction(instruction)
        }

        val isRestored = savedInstanceState.getBoolean(isRestoredKey)
        if (isNewDestination) {
            navigationController.onComposeDestinationAttached(
                destination,
                if (isRestored) savedInstanceState else null
            )
        }
        if (destination.getNavigationHandleViewModel().navigationContext == null) {
            navigationController.onComposeDestinationAttached(
                destination,
                if (isRestored) savedInstanceState else null
            )
        }
        return destination
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun EnroContainer(
    modifier: Modifier = Modifier,
    state: EnroContainerState = rememberEnroContainerState(),
) {
    val localViewModelStoreOwner = LocalViewModelStoreOwner.current!!
    DisposableEffect(state) {
        localViewModelStoreOwner.composableManger.enroContainers += state
        onDispose { localViewModelStoreOwner.composableManger.enroContainers -= state }
    }
    val backstack = state.observeBackstackAsState()
    val previous = state.previousState.observeAsState()
    val visible = backstack.lastOrNull()
    val toRender = previous.value.let {
        when {
            it == null -> backstack
            backstack.contains(it) -> backstack
            else -> backstack + it
        }
    }

    // If we're in the first render, we're going to skip animations
    val isFirstRender = remember { mutableStateOf(true) }
    val context = LocalContext.current as FragmentActivity
    val animations = remember(visible) {
        if (isFirstRender.value) {
            isFirstRender.value = false
            return@remember DefaultAnimations.none
        }
        animationsFor(
            context.navigationContext,
            if (previous.value == null) visible ?: NavigationInstruction.Close else NavigationInstruction.Close
        )
    }
    val saveableStateHolder = rememberSaveableStateHolder()

    if(toRender.isEmpty()) {
        Box(modifier = modifier) {}
        return
    }

    Box(modifier = modifier) {
        CompositionLocalProvider(
            LocalEnroContainerState provides state
        ) {
            toRender.forEach {
                key(it.instructionId) {
                    saveableStateHolder.SaveableStateProvider(it.instructionId) {
                        EnroAnimatedVisibility(
                            visible = it == visible,
                            animations = animations
                        ) {
                            DisposableEffect(true) {
                                onDispose {
                                    if (state.previousState.value == it) {
                                        state.previousState.value = null
                                    }
                                }
                            }
                            state.getDestination(it).InternalRender()
                        }
                    }
                }
            }
        }
    }
    remember(visible) {
        val destination = state.destinations[visible?.instructionId] ?: return@remember true
        state.navigationController.onComposeDestinationActive(destination)
        true
    }
}