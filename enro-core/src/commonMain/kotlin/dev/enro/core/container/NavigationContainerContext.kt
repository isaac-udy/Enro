package dev.enro.core.container

import androidx.savedstate.SavedState
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

    public fun save(): SavedState
    public fun restore(bundle: SavedState)
}