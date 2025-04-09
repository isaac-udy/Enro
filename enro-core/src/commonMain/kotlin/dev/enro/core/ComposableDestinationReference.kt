package dev.enro.core

/**
 * Interface defining the minimal requirements for a ComposableDestination
 * to be used by platform-independent code.
 */
public interface ComposableDestinationReference {
    /**
     * The NavigationContext associated with this composable destination
     */
    public val navigationContext: NavigationContext<*>
}