package dev.enro.core.container

public sealed class EmptyBehavior {
    /**
     * The container closes the last destination and becomes empty.
     */
    public data object AllowEmpty : EmptyBehavior()

    /**
     * When the container is about to become empty, the container keeps the last destination
     * and executes a Close for the last destination of the parent container (the owner of
     * the container). When the parent is the root context, the Activity is finished.
     *
     * In enro-compat, CloseParent and [ForceCloseParent] have the same behaviour.
     */
    public data object CloseParent : EmptyBehavior()

    /**
     * When the container is about to become empty, the container keeps the last destination
     * and executes a Close for the last destination of the parent container (the owner of
     * the container). When the parent is the root context, the Activity is finished.
     *
     * In enro-compat, ForceCloseParent and [CloseParent] have the same behaviour.
     */
    public data object ForceCloseParent : EmptyBehavior()

    /**
     * When the container is about to become empty, the container calls [onEmpty]. When
     * [onEmpty] returns `true`, the container keeps the last destination. When [onEmpty]
     * returns `false`, the container closes the last destination and becomes empty.
     */
    public class Action(
        public val onEmpty: () -> Boolean
    ) : EmptyBehavior()
}