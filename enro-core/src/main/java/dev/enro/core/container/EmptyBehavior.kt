package dev.enro.core.container

sealed class EmptyBehavior {
    /**
     * When this container is about to become empty, allow this container to become empty
     */
    object AllowEmpty : EmptyBehavior()

    /**
     * When this container is about to become empty, do not close the NavigationDestination in the
     * container, but instead close the parent NavigationDestination (i.e. the owner of this container)
     */
    object CloseParent : EmptyBehavior()

    /**
     * When this container is about to become empty, execute an action. If the result of the action function is
     * "true", then the action is considered to have consumed the request to become empty, and the container
     * will not close the last navigation destination. When the action function returns "false", the default
     * behaviour will happen, and the container will become empty.
     */
    class Action(
            val onEmpty: () -> Boolean
    ) : EmptyBehavior()
}