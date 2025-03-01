package dev.enro.core.container

/**
 * [EmptyBehavior] defines the behavior that should occur when a [NavigationContainer] would become
 * empty if the container is about to become empty. This allows a container to instead close it's
 * parent, or perform some other action instead (such as making another container active).
 */
public sealed class EmptyBehavior {
    /**
     * When this container is about to become empty, allow this container to become empty
     */
    public data object AllowEmpty : EmptyBehavior()

    /**
     * When this container is about to become empty, do not close the NavigationDestination in the
     * container, but instead request a close of the parent NavigationDestination (i.e. the owner of this container)
     *
     * This calls "requestClose" on the parent, not "close", so that the parent has an opportunity to
     * intercept the close functionality. If you want to *force* the parent container to close, and
     * not allow the parent container to intercept the close request, use [ForceCloseParent] instead.
     */
    public data object CloseParent : EmptyBehavior()

    /**
     * When this container is about to become empty, do not close the NavigationDestination in the
     * container, but instead force the parent NavigationDestination to close (i.e. the owner of this container).
     *
     * This calls "close" on the parent, rather than request close, so that the parent has no opportunity to
     * intercept the close with onCloseRequested. If you want to allow the parent container to be able
     * to intercept the close request, use [CloseParent] instead.
     */
    public data object ForceCloseParent : EmptyBehavior()

    /**
     * When this container is about to become empty, execute an action. If the result of the action function is
     * "true", then the action is considered to have consumed the request to become empty, and the container
     * will not close the last navigation destination. When the action function returns "false", the default
     * behaviour will happen, and the container will become empty.
     *
     * @returns true to keep the destination in the container, false to allow the container to become empty
     */
    public class Action(
        public val onEmpty: () -> Boolean
    ) : EmptyBehavior()
}