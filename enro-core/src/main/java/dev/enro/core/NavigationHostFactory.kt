package dev.enro.core

import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.GetNavigationBinding

@AdvancedEnroApi
public abstract class NavigationHostFactory<HostType: Any>(
    public val hostType: Class<HostType>,
) {
    internal lateinit var dependencyScope: EnroDependencyScope

    private val getNavigationBinding: GetNavigationBinding by lazy { dependencyScope.get() }

    protected fun getNavigationBinding(instruction: NavigationInstruction.Open<*>): NavigationBinding<*, *>?
        = getNavigationBinding.invoke(instruction)

    protected fun requireNavigationBinding(instruction: NavigationInstruction.Open<*>): NavigationBinding<*, *>
            = getNavigationBinding.require(instruction)

    protected fun cannotCreateHost(instruction: NavigationInstruction.Open<*>): Nothing {
        throw EnroException.CannotCreateHostForType(hostType, instruction.internal.openingType)
    }

    public abstract fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): Boolean

    public abstract fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*>
}

@AdvancedEnroApi
public interface NavigationHost {
    public fun accept(instruction: NavigationInstruction.Open<*>): Boolean = true
}