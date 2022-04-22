package dev.enro.core.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.*
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.compose.container.registerState
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainerBackstack
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import java.util.*

@Composable
fun rememberNavigationContainer(
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
fun rememberNavigationContainer(
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

    val id = rememberSaveable {
        UUID.randomUUID().toString()
    }

    val saveableStateHolder = rememberSaveableStateHolder()
    val controller = remember {
        ComposableNavigationContainer(
            id = id,
            parentContext = viewModelStoreOwner.getNavigationHandleViewModel().navigationContext!!,
            accept = accept,
            emptyBehavior = emptyBehavior,
            saveableStateHolder = saveableStateHolder
        )
    }

    viewModelStoreOwner.getNavigationHandleViewModel().navigationContext!!.containerManager.registerState(controller)
    DisposableEffect(controller.id) {
        if(controller.backstackFlow.value.backstack.isEmpty()) {
            val backstack = NavigationContainerBackstack(
                backstack = initialBackstack,
                exiting = null,
                exitingIndex = -1,
                lastInstruction = initialBackstack.lastOrNull() ?: NavigationInstruction.Close,
                isDirectUpdate = true
            )
            controller.setBackstack(backstack)
        }
        onDispose {  }
    }
    return controller
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun EnroContainer(
    modifier: Modifier = Modifier,
    controller: ComposableNavigationContainer = rememberNavigationContainer(),
) {
    key(controller.id) {
        controller.saveableStateHolder.SaveableStateProvider(controller.id) {
            val backstackState by controller.backstackFlow.collectAsState()

            Box(modifier = modifier) {
                backstackState.renderable.forEach {
                    key(it.instructionId) {
                        controller.getDestinationContext(it).Render()
                    }
                }
            }
        }
    }
}

