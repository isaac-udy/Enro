package dev.enro.core.fragment.container

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainerProperty
import dev.enro.core.container.createRootBackStack
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.navigationContext


public fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey.SupportsPush? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty<FragmentNavigationContainer> = navigationContainer(
    containerId = containerId,
    rootInstruction = { root()?.let { NavigationInstruction.Push(it) } },
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    accept = accept,
)

@JvmName("navigationContainerFromInstruction")
public fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    rootInstruction: () -> NavigationInstruction.Open<NavigationDirection.Push>?,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty<FragmentNavigationContainer> = NavigationContainerProperty(
    lifecycleOwner = this,
    navigationContainerProducer = {
        FragmentNavigationContainer(
            containerId = containerId,
            parentContext = navigationContext,
            accept = accept,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            initialBackstackState = createRootBackStack(rootInstruction())
        )
    }
)

public fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey.SupportsPush? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty<FragmentNavigationContainer> = navigationContainer(
    containerId = containerId,
    rootInstruction = { root()?.let { NavigationInstruction.Push(it) } },
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    accept = accept,
)

@JvmName("navigationContainerFromInstruction")
public fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    rootInstruction: () -> NavigationInstruction.Open<NavigationDirection.Push>?,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty<FragmentNavigationContainer> = NavigationContainerProperty(
    lifecycleOwner = this,
    navigationContainerProducer = {
        FragmentNavigationContainer(
            containerId = containerId,
            parentContext = navigationContext,
            accept = accept,
            emptyBehavior = emptyBehavior,
            interceptor = interceptor,
            initialBackstackState = createRootBackStack(rootInstruction())
        )
    }
)