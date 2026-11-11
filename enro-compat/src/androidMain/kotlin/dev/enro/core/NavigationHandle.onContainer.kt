package dev.enro.core

import dev.enro.NavigationBackstack

private const val ON_CONTAINER_NOT_SUPPORTED =
    "Not supported in Enro 3: the block is never run. Get the NavigationContainer from the navigation context and update its backstack directly."

@Deprecated(ON_CONTAINER_NOT_SUPPORTED)
public fun dev.enro.NavigationHandle<*>.onContainer(
    key: NavigationContainerKey,
    block: OnActiveContainerScope.() -> Unit
) {

}

@Deprecated(ON_CONTAINER_NOT_SUPPORTED)
public fun dev.enro.NavigationHandle<*>.onParentContainer(
    block: OnActiveContainerScope.() -> Unit
) {

}

@Deprecated(ON_CONTAINER_NOT_SUPPORTED)
public fun dev.enro.NavigationHandle<*>.onActiveContainer(
    block: OnActiveContainerScope.() -> Unit
) {

}

@Deprecated(ON_CONTAINER_NOT_SUPPORTED)
public class OnActiveContainerScope(
    public val backstack: NavigationBackstack
) {
    public fun setBackstack(backstack: NavigationBackstack) {}
}
