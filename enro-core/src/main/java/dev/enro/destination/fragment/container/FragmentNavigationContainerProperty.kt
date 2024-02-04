package dev.enro.core.fragment.container

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainerProperty
import dev.enro.core.container.NavigationInstructionFilter
import dev.enro.core.container.acceptAll
import dev.enro.core.container.backstackOfNotNull
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.navigationContext


public fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey.SupportsPush? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerProperty<FragmentNavigationContainer> = navigationContainer(
    containerId = containerId,
    rootInstruction = { root()?.let { NavigationInstruction.Push(it) } },
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    animations = animations,
    filter = filter,
)

@JvmName("navigationContainerFromInstruction")
public fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    rootInstruction: () -> NavigationInstruction.Open<NavigationDirection.Push>?,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerProperty<FragmentNavigationContainer> = NavigationContainerProperty(
    lifecycleOwner = this,
    navigationContainerProducer = {
        FragmentNavigationContainer(
            containerId = containerId,
            parentContext = navigationContext,
            filter = filter,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            animations = animations,
            initialBackstack = backstackOfNotNull(rootInstruction())
        )
    }
)

public fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey.SupportsPush? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerProperty<FragmentNavigationContainer> = navigationContainer(
    containerId = containerId,
    rootInstruction = { root()?.let { NavigationInstruction.Push(it) } },
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    animations = animations,
    filter = filter,
)

@JvmName("navigationContainerFromInstruction")
public fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    rootInstruction: () -> NavigationInstruction.Open<NavigationDirection.Push>?,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerProperty<FragmentNavigationContainer> = NavigationContainerProperty(
    lifecycleOwner = this,
    navigationContainerProducer = {
        FragmentNavigationContainer(
            containerId = containerId,
            parentContext = navigationContext,
            filter = filter,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            animations = animations,
            initialBackstack = backstackOfNotNull(rootInstruction())
        )
    }
)
