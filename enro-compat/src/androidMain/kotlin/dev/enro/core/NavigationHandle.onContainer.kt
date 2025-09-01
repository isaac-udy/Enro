package dev.enro.core

import dev.enro.NavigationBackstack

public fun dev.enro.NavigationHandle<*>.onContainer(
    key: NavigationContainerKey,
    block: OnActiveContainerScope.() -> Unit
) {

}

public fun dev.enro.NavigationHandle<*>.onParentContainer(
    block: OnActiveContainerScope.() -> Unit
) {

}

public fun dev.enro.NavigationHandle<*>.onActiveContainer(
    block: OnActiveContainerScope.() -> Unit
) {

}

public class OnActiveContainerScope(
    public val backstack: NavigationBackstack
) {
    public fun setBackstack(backstack: NavigationBackstack) {}
}
