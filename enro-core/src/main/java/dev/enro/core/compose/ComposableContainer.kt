package dev.enro.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.*
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstackState
import dev.enro.core.container.createRootBackStack
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import java.util.*

@Composable
public fun rememberNavigationContainer(
    root: NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        initialBackstackState = rememberSaveable(saver = NavigationBackstackState.Saver) {
            createRootBackStack(
                NavigationInstruction.Push(root)
            )
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
    return rememberNavigationContainer(
        initialBackstackState = rememberSaveable(saver = NavigationBackstackState.Saver) {
            createRootBackStack(
                initialState.map {
                    NavigationInstruction.Push(it)
                }
            )
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
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        initialBackstackState = rememberSaveable(saver = NavigationBackstackState.Saver) { createRootBackStack(initialBackstack) },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        accept = accept,
    )
}

@Composable
@AdvancedEnroApi
public fun rememberNavigationContainer(
    id: String = rememberSaveable { UUID.randomUUID().toString() },
    initialBackstackState: NavigationBackstackState,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
    saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder(),
): ComposableNavigationContainer {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current!!

    val controller = remember {
        ComposableNavigationContainer(
            id = id,
            parentContext = viewModelStoreOwner.navigationContext!!,
            accept = accept,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            initialBackstackState = initialBackstackState
        ).apply {
            this.saveableStateHolder = saveableStateHolder
        }
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

