package dev.enro.core.container.components

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.CanInstructionBeHostedAs
import kotlin.reflect.KClass

public interface ContainerAcceptPolicy {
    public fun accepts(instruction: AnyOpenInstruction): Boolean
    public fun accepts(navigationKey: NavigationKey): Boolean

    public class Default(
        private val context: NavigationContext<*>,
        private val acceptsContextType: KClass<out Any>,
        private val acceptsNavigationKey: (NavigationKey) -> Boolean
    ) : ContainerAcceptPolicy {
        private val canInstructionBeHostedAs = context.controller.dependencyScope.get<CanInstructionBeHostedAs>()

        public override fun accepts(navigationKey: NavigationKey): Boolean {
            return acceptsNavigationKey(navigationKey)
        }

        public override fun accepts(
            instruction: AnyOpenInstruction
        ): Boolean {
            return (accepts(instruction.navigationKey) || instruction.navigationDirection == NavigationDirection.Present)
                    && acceptedByContext(instruction)
                    && canInstructionBeHostedAs(
                hostType = acceptsContextType.java,
                navigationContext = context,
                instruction = instruction
            )
        }

        private fun acceptedByContext(navigationInstruction: NavigationInstruction.Open<*>): Boolean {
            if (context.contextReference !is NavigationHost) return true
            return context.contextReference.accept(navigationInstruction)
        }
    }
}

