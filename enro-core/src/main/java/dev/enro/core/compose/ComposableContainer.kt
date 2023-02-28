package dev.enro.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.*
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.compose.container.ContainerRegistrationStrategy
import dev.enro.core.compose.destination.ComposableDestinationOwner
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
@AdvancedEnroApi
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    initialBackstack: NavigationBackstack,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current!!

    val controller = remember {
        // TODO would be nice if we didn't need to do this?
        val context = when(viewModelStoreOwner) {
            is ComposableDestinationOwner -> viewModelStoreOwner.destination.context
            else -> viewModelStoreOwner.navigationContext!!
        }

        val existingContainer = context.containerManager.getContainer(key) as? ComposableNavigationContainer
        existingContainer ?: ComposableNavigationContainer(
            key = key,
            parentContext = context,
            accept = accept,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            initialBackstack = initialBackstack
        )
    }
    controller.registerWithContainerManager(
        when(key) {
            is NavigationContainerKey.Dynamic -> ContainerRegistrationStrategy.DisposeWithComposition
            is NavigationContainerKey.FromId -> ContainerRegistrationStrategy.DisposeWithLifecycle
            is NavigationContainerKey.FromName -> ContainerRegistrationStrategy.DisposeWithLifecycle
        }
    )
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
    container: ComposableNavigationContainer = rememberNavigationContainer(
        initialBackstack = emptyBackstack()
    ),
) {
    Box(modifier = modifier) {
        container.Render()
    }
}

