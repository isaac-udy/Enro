package dev.enro.core.compose

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.toSize
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.core.*
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

internal val LocalEnroContainerState = compositionLocalOf<EnroContainerState> {
    throw IllegalStateException("The current composition does not have a ComposableContainer attached")
}

@Composable
fun rememberEnroContainerState(
    initialState: List<NavigationInstruction.Open> = emptyList(),
    accept: (NavigationKey) -> Boolean = { true }
): EnroContainerState {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current!!
    val id = rememberSaveable {
        UUID.randomUUID().toString()
    }
    return remember {
        EnroContainerState(
            initialState = initialState,
            id = id,
            navigationHandle = viewModelStoreOwner.getNavigationHandleViewModel(),
            accept = accept
        )
    }
}

@Parcelize
data class EnroContainerBackstackEntry(
    val instruction: NavigationInstruction.Open,
    val previouslyActiveContainerId: String?
) : Parcelable

data class EnroContainerBackstackState(
    val lastInstruction: NavigationInstruction,
    val backstackEntries: List<EnroContainerBackstackEntry>,
    val exiting: NavigationInstruction.Open?,
    val exitingIndex: Int
) {
    val backstack = backstackEntries.map { it.instruction }
    val visible: NavigationInstruction.Open? = backstack.lastOrNull()
    val renderable: List<NavigationInstruction.Open> = run {
        if(exiting == null) return@run backstack
        if(backstack.contains(exiting)) return@run backstack
        if(exitingIndex > backstack.lastIndex) return@run backstack + exiting
        return@run backstack.flatMapIndexed { index, open ->
            if(exitingIndex == index) return@flatMapIndexed listOf(exiting, open)
            return@flatMapIndexed listOf(open)
        }
    }

    fun push(
        instruction: NavigationInstruction.Open,
        activeContainerId: String?
    ): EnroContainerBackstackState {
        return when (instruction.navigationDirection) {
            NavigationDirection.FORWARD -> {
                copy(
                    backstackEntries = backstackEntries + EnroContainerBackstackEntry(
                        instruction,
                        activeContainerId
                    ),
                    exiting = visible,
                    exitingIndex = backstack.lastIndex,
                    lastInstruction = instruction
                )
            }
            NavigationDirection.REPLACE -> {
                copy(
                    backstackEntries = backstackEntries.dropLast(1) + EnroContainerBackstackEntry(
                        instruction,
                        activeContainerId
                    ),
                    exiting = visible,
                    exitingIndex = backstack.lastIndex,
                    lastInstruction = instruction
                )
            }
            NavigationDirection.REPLACE_ROOT -> {
                copy(
                    backstackEntries = listOf(
                        EnroContainerBackstackEntry(
                            instruction,
                            activeContainerId
                        )
                    ),
                    exiting = visible,
                    exitingIndex = 0,
                    lastInstruction = instruction
                )
            }
        }
    }

    fun close(): EnroContainerBackstackState {
        return copy(
            backstackEntries = backstackEntries.dropLast(1),
            exiting = visible,
            exitingIndex = backstack.lastIndex,
            lastInstruction = NavigationInstruction.Close
        )
    }
}

class EnroContainerState internal constructor(
    initialState: List<NavigationInstruction.Open> = emptyList(),
    val id: String,
    val navigationHandle: NavigationHandle,
    val accept: (NavigationKey) -> Boolean = { true }
) {
    private val backstackState = MutableStateFlow(
        EnroContainerBackstackState(
            backstackEntries = initialState.map { EnroContainerBackstackEntry(it, null) },
            exiting = null,
            exitingIndex = -1,
            lastInstruction = initialState.lastOrNull() ?: NavigationInstruction.Close
        )
    )
    val backstack get() = backstackState.value.backstack

    internal val navigationController: NavigationController = navigationHandle.controller
    internal val navigationContext: NavigationContext<*> =
        (navigationHandle as NavigationHandleViewModel).navigationContext!!

    private val destinations = mutableMapOf<String, ComposableDestination>()
    private val currentDestination get() = backstackState.value.visible?.instructionId?.let { destinations[it] }

    internal var size: Size = Size.Zero

    internal val activeContext: NavigationContext<*>? get() = currentDestination?.getNavigationHandleViewModel()?.navigationContext

    fun push(instruction: NavigationInstruction.Open) {
        backstackState.value = backstackState.value.push(
            instruction,
            navigationContext.childComposableManager.primaryContainer?.id
        )
        navigationContext.childComposableManager.setPrimaryContainer(id)
    }

    fun close() {
        currentDestination ?: return

        navigationContext.childComposableManager.setPrimaryContainer(backstackState.value.backstackEntries.lastOrNull()?.previouslyActiveContainerId)
        backstackState.value = backstackState.value.close()
    }

    internal fun onInstructionDisposed(instruction: NavigationInstruction.Open) {
        if (backstackState.value.exiting == instruction) {
            backstackState.value = backstackState.value.copy(
                exiting = null,
                exitingIndex = -1
            )
        }
    }

    @Composable
    fun observeBackstackState(): EnroContainerBackstackState {
        val backstack = backstackState.collectAsState()
        return backstack.value
    }

    @Composable
    fun persist(): EnroContainerState {
        val backstack = backstackState.collectAsState()
        val savedBackStack = rememberSaveable(backstack.value, key = id) {
            ArrayList(backstack.value.backstackEntries)
        }

        if (backstackState.value.backstackEntries != savedBackStack) {
            backstackState.value = EnroContainerBackstackState(
                backstackEntries = savedBackStack,
                exiting = null,
                exitingIndex = -1,
                lastInstruction = backstackState.value.visible ?: NavigationInstruction.Close
            )
        }
        return this
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
        destination.containerState = LocalEnroContainerState.current

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
    state.persist()
    localComposableManager.registerState(state)
    val backstackState = state.observeBackstackState()
    val saveableStateHolder = rememberSaveableStateHolder()

    val context = localEnroContext
    val animations = remember(backstackState.visible) {
        animationsFor(
            context,
            backstackState.lastInstruction
        )
    }

    Box(modifier = modifier.onGloballyPositioned {
        state.size = it.size.toSize()
    }) {
        CompositionLocalProvider(
            LocalEnroContainerState provides state
        ) {
            backstackState.renderable.forEach {
                key(it.instructionId) {
                    EnroAnimatedVisibility(
                        visible = it == backstackState.visible,
                        animations = animations
                    ) {
                        saveableStateHolder.SaveableStateProvider(it.instructionId) {
                            val destination = state.getDestination(it)
                            destination.InternalRender()

                            DisposableEffect(true) {
                                state.navigationController.onComposeDestinationActive(destination)
                                onDispose {
                                    state.onInstructionDisposed(it)
                                    state.navigationController.onComposeDestinationClosed(
                                        destination
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}