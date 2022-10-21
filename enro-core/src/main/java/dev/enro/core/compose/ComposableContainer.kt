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
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.createRootBackStack
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import java.util.*

@Composable
public fun rememberNavigationContainer(
    root: NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        initialState = rememberSaveable {
            listOf(root)
        },
        emptyBehavior = emptyBehavior,
        accept = accept
    )
}

@Composable
public fun rememberNavigationContainer(
    initialState: List<NavigationKey.SupportsPush> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberEnroContainerController(
        initialBackstack = rememberSaveable {
            initialState.map {
                NavigationInstruction.Push(it)
            }
        },
        emptyBehavior = emptyBehavior,
        accept = accept
    )
}

@Composable
@Deprecated("Use the rememberEnroContainerController that takes a List<NavigationKey> instead of a List<NavigationInstruction.Open>")
public fun rememberEnroContainerController(
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

    controller.registerWithContainerManager()
    return controller
}

@Composable
public fun EnroContainer(
    modifier: Modifier = Modifier,
    container: ComposableNavigationContainer = rememberNavigationContainer(),
) {
    key(container.id) {
        container.saveableStateHolder.SaveableStateProvider(container.id) {
            val backstackState by container.backstackFlow.collectAsState()

            Box(modifier = modifier) {
                backstackState.renderable
                    .mapNotNull { container.getDestinationOwner(it) }
                    .forEach {
                        it.Render(backstackState)
                    }
            }
        }
    }
}

