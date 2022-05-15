package dev.enro.core.destinations

/**
 * This object contains marker interfaces which are used by tests to create
 */
object TestDestination {
    /**
     * Marks a destination as being able to be opened into the primary container of a root destination
     */
    interface IntoPrimaryContainer

    /**
     * Marks a destination as being able to be opened into the secondary container of a root destination
     */
    interface IntoSecondaryContainer

    /**
     * Marks a destination as being able to be opened into the primary container of any non-root destination
     */
    interface IntoPrimaryChildContainer

    /**
     * Marks a destination as being able to be opened into the secondary container of any non-root destination
     */
    interface IntoSecondaryChildContainer
}