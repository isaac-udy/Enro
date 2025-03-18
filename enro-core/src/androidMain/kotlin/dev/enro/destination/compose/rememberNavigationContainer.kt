package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.compose.container.ContainerRegistrationStrategy
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationInstructionFilter
import dev.enro.core.container.acceptAll
import dev.enro.core.container.backstackOf
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.requireNavigationContext

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    root: NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = rememberSaveable {
            backstackOf(NavigationInstruction.Push(root))
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
        filter = filter,
    )
}

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    initialBackstack: List<NavigationKey.SupportsPush> = emptyList(),
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = rememberSaveable {
            initialBackstack.map {
                NavigationInstruction.Push(it)
            }.toBackstack()
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
        filter = filter,
    )
}

@Composable
@AdvancedEnroApi
public fun rememberNavigationContainer(
    key: NavigationContainerKey = rememberSaveable { NavigationContainerKey.Dynamic() },
    initialBackstack: NavigationBackstack,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
    registrationStrategy: ContainerRegistrationStrategy = remember(key) {
        when(key) {
            is NavigationContainerKey.Dynamic -> ContainerRegistrationStrategy.DisposeWithComposition
            else -> ContainerRegistrationStrategy.DisposeWithLifecycle
        }
    }
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
            instructionFilter = filter,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            animations = animations,
        )
    }
    LaunchedEffect(emptyBehavior) {
        navigationContainer.emptyBehavior = emptyBehavior
    }
    navigationContainer.registerWithContainerManager(registrationStrategy, initialBackstack)
    return navigationContainer
}
