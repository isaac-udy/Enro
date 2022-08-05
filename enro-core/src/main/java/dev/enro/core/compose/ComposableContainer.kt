package dev.enro.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.compose.container.registerState
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.createRootBackStack
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import java.util.*

@Composable
fun rememberNavigationContainer(
    root: NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
) : ComposableNavigationContainer {
    return rememberEnroContainerController(
        initialBackstack = listOf(NavigationInstruction.Push(root)),
        emptyBehavior = emptyBehavior,
        accept = accept
    )
}

@Composable
fun rememberNavigationContainer(
    initialState: List<NavigationKey.SupportsPush> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
) : ComposableNavigationContainer {
    return rememberEnroContainerController(
        initialBackstack = initialState.mapIndexed { i, it ->
            NavigationInstruction.Push(it)
        },
        emptyBehavior = emptyBehavior,
        accept = accept
    )
}

@Composable
@Deprecated("Use the rememberEnroContainerController that takes a List<NavigationKey> instead of a List<NavigationInstruction.Open>")
fun rememberEnroContainerController(
    initialBackstack: List<AnyOpenInstruction> = emptyList(),
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
            saveableStateHolder = saveableStateHolder,
            initialBackstack = createRootBackStack(initialBackstack)
        )
    }

    viewModelStoreOwner.getNavigationHandleViewModel().navigationContext!!.containerManager.registerState(controller)
    return controller
}

@Composable
fun EnroContainer(
    modifier: Modifier = Modifier,
    container: ComposableNavigationContainer = rememberNavigationContainer(),
) {
    key(container.id) {
        container.saveableStateHolder.SaveableStateProvider(container.id) {
            val backstackState by container.backstackFlow.collectAsState()

            Box(modifier = modifier) {
                backstackState.renderable
                    .mapNotNull { container.getDestinationContext(it) }
                    .forEach {
                        it.Render()
                    }
            }
        }
    }
}

