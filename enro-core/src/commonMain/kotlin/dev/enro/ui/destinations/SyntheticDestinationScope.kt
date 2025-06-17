package dev.enro.ui.destinations

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.context.AnyNavigationContext
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext

// TODO: Need to add functionality to do a replace/close/complete/completeFrom from the context of
//  a synthetic destination scope (to allow for "forwardResult" and "setResult") type functionality
//  that previously existed in Enro 2.x
public class SyntheticDestinationScope<K : NavigationKey>(
    // context is the NavigationContext that is executing this SyntheticDestination,
    // which could be a RootContext, ContainerContext or DestinationContext depending on how
    // the synthetic destination was opened
    public val context: NavigationContext,
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K = instance.key

    // destinationContext will be the active destination closest to the context,
    // meaning that if context is a DestinationContext, destinationContext will be that instance,
    // if context is a ContainerContext, destinationContext will be that container's active context,
    // and if the context is a RootContext, destinationContext will be the active child of the RootContext's
    // active ContainerContext
    public val destinationContext: DestinationContext<NavigationKey>?
        get() = when(context) {
            is DestinationContext<*> -> context
            is ContainerContext -> context.activeChild
            is RootContext -> context.activeChild?.activeChild
        }

    @Deprecated("Use destinationContext or context instead for greater clarity about the context being used")
    public val navigationContext: AnyNavigationContext
        get() = context

}
