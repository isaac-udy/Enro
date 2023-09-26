package dev.enro.core.container.components

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.container.NavigationBackstack

public interface ContainerContextProvider <T: Any>{
    public fun getActiveNavigationContext(backstack: NavigationBackstack): NavigationContext<out T>?
    public fun getContext(instruction: AnyOpenInstruction): T?
    public fun createContext(instruction: AnyOpenInstruction): T

    public fun bind(state: ContainerState)
    public fun destroy()
}

public fun <T : Any> ContainerContextProvider<T>.getOrCreateContext(instruction: AnyOpenInstruction): T {
    return getContext(instruction) ?: createContext(instruction)
}
