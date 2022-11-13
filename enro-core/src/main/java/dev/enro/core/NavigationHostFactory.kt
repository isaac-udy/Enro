package dev.enro.core

public interface NavigationHostFactory {
    public fun canCreateHostFor(targetContextType: Class<*>, binding: NavigationBinding<*, *>): Boolean
    public fun createHostFor(targetContextType: Class<*>, instruction: NavigationInstruction.Open<*>): NavigationInstruction.Open<*>
}

public inline fun <reified T: Any> NavigationHostFactory.canCreateHostFor(binding: NavigationBinding<*, *>): Boolean {
    return canCreateHostFor(T::class.java, binding)
}

public inline fun <reified T: Any> NavigationHostFactory.createHostFor(instruction: NavigationInstruction.Open<*>): NavigationInstruction.Open<*> {
    return createHostFor(T::class.java, instruction)
}