package dev.enro.core

@AdvancedEnroApi
public interface NavigationHostFactory<HostType: Any> {
    public val hostType: Class<HostType>

    public fun supports(instruction: NavigationInstruction.Open<*>): Boolean
    public fun wrap(instruction: NavigationInstruction.Open<*>): NavigationInstruction.Open<*>
}

internal fun <T: Any> NavigationHostFactory<T>.cannotCreateHost(instruction: NavigationInstruction.Open<*>): Nothing {
    throw EnroException.CannotCreateHostForType(hostType, instruction.internal.openingType)
}

@AdvancedEnroApi
public interface NavigationHost