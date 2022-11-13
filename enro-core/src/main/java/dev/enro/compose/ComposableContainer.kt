package dev.enro.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.compose.container.ComposableNavigationContainer
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.createRootBackStack
import dev.enro.core.getNavigationContext
import dev.enro.core.interceptor.builder.NavigationInterceptorBuilder
import java.util.*

@Composable
public fun rememberNavigationContainer(
    root: NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        initialState = rememberSaveable {
            listOf(root)
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        accept = accept
    )
}

@Composable
public fun rememberNavigationContainer(
    initialState: List<NavigationKey.SupportsPush> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberEnroContainerController(
        initialBackstack = rememberSaveable {
            initialState.map {
                NavigationInstruction.Push(it)
            }
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        accept = accept
    )
}

@Composable
@Deprecated("Use the rememberEnroContainerController that takes a List<NavigationKey> instead of a List<NavigationInstruction.Open>")
public fun rememberEnroContainerController(
    initialBackstack: List<AnyOpenInstruction> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
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
            parentContext = viewModelStoreOwner.getNavigationContext(),
            accept = accept,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            saveableStateHolder = saveableStateHolder,
            initialBackstackState = createRootBackStack(initialBackstack)
        )
    }

    controller.registerWithContainerManager()
    return controller
}

@Composable
@Deprecated(
    message = "Please use ComposableNavigationContainer.Render() directly, and wrap this inside of a Box() or other layout if you wish to provide modifiers",
    replaceWith = ReplaceWith(
        "Box(modifier = modifier) { container.Render() }",
        "androidx.compose.foundation.layout.Box"
    )
)
public fun EnroContainer(
    modifier: Modifier = Modifier,
    container: ComposableNavigationContainer = rememberNavigationContainer(),
) {
    Box(modifier = modifier) {
        container.Render()
    }
}

