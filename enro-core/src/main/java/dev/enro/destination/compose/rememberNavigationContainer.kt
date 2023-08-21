package dev.enro.destination.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.*
import dev.enro.destination.compose.container.ComposableNavigationContainer
import dev.enro.destination.compose.container.ContainerRegistrationStrategy
import dev.enro.core.container.*
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    root: NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = rememberSaveable {
            backstackOf(NavigationInstruction.Push(root))
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
        accept = accept
    )
}

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    initialState: List<NavigationKey.SupportsPush> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
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
        animations = animations,
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
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    val localNavigationHandle = navigationHandle()
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // The navigation context attached to a NavigationHandle may change when the Context, View,
    // or LifecycleOwner changes, so we're going to re-query the navigation context whenever
    // any of these change, to ensure the container always has an up-to-date NavigationContext
    val localNavigationContext = remember(context, view, lifecycleOwner) {
        localNavigationHandle.requireNavigationContext()
    }
    val navigationContainer = remember(localNavigationContext.containerManager) {
        val existingContainer = localNavigationContext.containerManager.getContainer(key) as? ComposableNavigationContainer
        existingContainer ?: ComposableNavigationContainer(
            key = key,
            parentContext = localNavigationContext,
            accept = accept,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            animations = animations,
            initialBackstack = initialBackstack,
        )
    }

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
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        initialBackstack = initialBackstack.toBackstack(),
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
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

