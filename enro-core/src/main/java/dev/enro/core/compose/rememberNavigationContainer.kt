package dev.enro.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import dev.enro.core.*
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.compose.container.ContainerRegistrationStrategy
import dev.enro.core.container.*
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    root: NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = rememberSaveable {
            backstackOf(NavigationInstruction.Push(root))
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        accept = accept
    )
}

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    initialState: List<NavigationKey.SupportsPush> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = rememberSaveable {
            initialState.map {
                NavigationInstruction.Push(it)
            }.toBackstack()
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        accept = accept
    )
}

@Composable
@AdvancedEnroApi
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    initialBackstack: NavigationBackstack,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    val localNavigationHandle = navigationHandle()
    val saveableStateHolder = rememberSaveableStateHolder()

    val navigationContainer = remember {
        val context = localNavigationHandle.requireNavigationContext()
        val existingContainer = context.containerManager.getContainer(key) as? ComposableNavigationContainer
        existingContainer ?: ComposableNavigationContainer(
            key = key,
            parentContext = context,
            accept = accept,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            initialBackstack = initialBackstack,
            saveableStateHolder = saveableStateHolder
        )
    }.also { it.saveableStateHolder = saveableStateHolder }

    navigationContainer.registerWithContainerManager(
        when(key) {
            is NavigationContainerKey.Dynamic -> ContainerRegistrationStrategy.DisposeWithComposition
            is NavigationContainerKey.FromId -> ContainerRegistrationStrategy.DisposeWithLifecycle
            is NavigationContainerKey.FromName -> ContainerRegistrationStrategy.DisposeWithLifecycle
        }
    )
    return navigationContainer
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
        initialBackstack = initialBackstack.toBackstack(),
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        accept = accept,
    )
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
    container: ComposableNavigationContainer = rememberNavigationContainer(
        initialBackstack = emptyBackstack()
    ),
) {
    Box(modifier = modifier) {
        container.Render()
    }
}

