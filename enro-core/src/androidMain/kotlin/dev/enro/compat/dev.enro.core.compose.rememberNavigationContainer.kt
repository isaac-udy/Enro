package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.asPush
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationInstructionFilter
import dev.enro.core.container.acceptAll
import dev.enro.core.container.backstackOf
import dev.enro.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.ui.NavigationContainerState


@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    root: dev.enro.core.NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerState {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = rememberSaveable {
            backstackOf(root.asPush())
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
        filter = filter,
    )
}

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    initialBackstack: List<dev.enro.core.NavigationKey.SupportsPush> = emptyList(),
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerState {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = rememberSaveable {
            initialBackstack.map {
                it.asPush()
            }
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
        filter = filter,
    )
}

@Composable
@AdvancedEnroApi
@JvmName("rememberNavigationContainerWithBackstack")
public fun rememberNavigationContainer(
    key: NavigationContainer.Key =  NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    initialBackstack: NavigationBackstack,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerState {
    TODO()
}