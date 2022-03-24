package dev.enro.core.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.core.*
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import java.util.*

internal class EnroDestinationStorage : ViewModel() {
    val destinations = mutableMapOf<String, MutableMap<String, ComposableDestinationContextReference>>()

    override fun onCleared() {
        destinations.values
            .flatMap { it.values }
            .forEach { it.viewModelStore.clear() }

        super.onCleared()
    }
}

@Composable
fun rememberEnroContainerController(
    root: NavigationKey,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
) : ComposableNavigationContainer {
    return rememberEnroContainerController(
        initialBackstack = listOf(NavigationInstruction.Replace(root)),
        emptyBehavior = emptyBehavior,
        accept = accept
    )
}

@Composable
fun rememberEnroContainerController(
    initialState: List<NavigationKey> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
) : ComposableNavigationContainer {
    return rememberEnroContainerController(
        initialBackstack = initialState.mapIndexed { i, it ->
            if(i == 0) NavigationInstruction.Replace(it)
            else NavigationInstruction.Forward(it)
        },
        emptyBehavior = emptyBehavior,
        accept = accept
    )
}

@Composable
@Deprecated("Use the rememberEnroContainerController that takes a List<NavigationKey> instead of a List<NavigationInstruction.Open>")
fun rememberEnroContainerController(
    initialBackstack: List<NavigationInstruction.Open> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
    ignore: Unit = Unit
): ComposableNavigationContainer {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current!!
    val destinationStorage = viewModel<EnroDestinationStorage>()

    val id = rememberSaveable {
        UUID.randomUUID().toString()
    }

    val saveableStateHolder = rememberSaveableStateHolder()
    val controller = remember {
        ComposableNavigationContainer(
            id = id,
            parentContext = viewModelStoreOwner.getNavigationHandleViewModel().navigationContext!!,
            accept = accept,
            destinationStorage = destinationStorage,
            emptyBehavior = emptyBehavior,
            saveableStateHolder = saveableStateHolder
        )
    }

    val savedBackstack = rememberSaveable(
        key = id,
        saver = EnroContainerBackstackStateSaver {
            controller.backstackFlow.value
        }
    ) {
        EnroContainerBackstackState(
            backstackEntries = initialBackstack.map { EnroContainerBackstackEntry(it, null) },
            exiting = null,
            exitingIndex = -1,
            lastInstruction = initialBackstack.lastOrNull() ?: NavigationInstruction.Close,
            skipAnimations = true
        )
    }

    viewModelStoreOwner.getNavigationHandleViewModel().navigationContext!!.containerManager.registerState(controller)
    return remember {
        controller.setBackstack(savedBackstack)
        controller
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun EnroContainer(
    modifier: Modifier = Modifier,
    controller: ComposableNavigationContainer = rememberEnroContainerController(),
) {
    key(controller.id) {
        controller.saveableStateHolder.SaveableStateProvider(controller.id) {
            val backstackState by controller.backstackFlow.collectAsState()

            Box(modifier = modifier) {
                backstackState.renderable.forEach {
                    key(it.instructionId) {
                        controller.getDestinationContext(it).Render()
                        controller.bindDestination(it)
                    }
                }
            }
        }
    }
}

