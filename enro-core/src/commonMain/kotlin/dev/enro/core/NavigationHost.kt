package dev.enro.core

// Interface to be implemented by platform-specific navigation hosts
public interface NavigationHost {
    public fun accept(instruction: NavigationInstruction.Open<*>): Boolean = true
}