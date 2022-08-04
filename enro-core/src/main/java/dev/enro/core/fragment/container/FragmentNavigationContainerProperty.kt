package dev.enro.core.fragment.container

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.*
import dev.enro.core.container.*


fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey.SupportsPush? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty<FragmentNavigationContainer> = navigationContainer(
    containerId = containerId,
    rootInstruction = { root()?.let { NavigationInstruction.Push(it) } },
    emptyBehavior = emptyBehavior,
    accept = accept,
)

@JvmName("navigationContainerFromInstruction")
fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    rootInstruction: () -> NavigationInstruction.Open<NavigationDirection.Push>?,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty<FragmentNavigationContainer> = NavigationContainerProperty(
    lifecycleOwner = this,
    navigationContainerProducer = {
        FragmentNavigationContainer(
            containerId = containerId,
            parentContext = navigationContext,
            accept = accept,
            emptyBehavior = emptyBehavior,
            initialBackstack = createRootBackStack(rootInstruction())
        )
    }
)

fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey.SupportsPush? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty<FragmentNavigationContainer> = navigationContainer(
    containerId = containerId,
    rootInstruction = { root()?.let { NavigationInstruction.Push(it) } },
    emptyBehavior = emptyBehavior,
    accept = accept,
)

@JvmName("navigationContainerFromInstruction")
fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    rootInstruction: () -> NavigationInstruction.Open<NavigationDirection.Push>?,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty<FragmentNavigationContainer> = NavigationContainerProperty(
    lifecycleOwner = this,
    navigationContainerProducer = {
        FragmentNavigationContainer(
            containerId = containerId,
            parentContext = navigationContext,
            accept = accept,
            emptyBehavior = emptyBehavior,
            initialBackstack = createRootBackStack(rootInstruction())
        )
    }
)