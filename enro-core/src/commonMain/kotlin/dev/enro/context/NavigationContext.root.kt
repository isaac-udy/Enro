package dev.enro.context

public fun AnyNavigationContext.root(): RootContext {
    return when(this) {
        is RootContext -> this
        is ContainerContext -> parent.root()
        is DestinationContext<*> -> parent.root()
    }
}