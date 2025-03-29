package dev.enro.core.container

import androidx.core.bundle.Bundle
import kotlinx.coroutines.flow.StateFlow

/**
 * NavigationContainerContext exists to act as a safe way to access certain properties and functionality of a NavigationContainer
 * from within NavigationInstructions and other places where full access to the NavigationContainer might introduce memory leaks
 * or cause issues with testability
 */
public interface NavigationContainerContext {
    public val backstackFlow: StateFlow<NavigationBackstack>
    public val backstack: NavigationBackstack
    public fun setBackstack(backstack: NavigationBackstack)

    public val isActive: Boolean
    public fun setActive()

    public fun save(): Bundle
    public fun restore(bundle: Bundle)
}