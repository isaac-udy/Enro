package dev.enro.core.controller.interceptor

import dev.enro.core.*
import dev.enro.core.activity.ActivityNavigator
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.controller.container.NavigatorContainer
import dev.enro.core.fragment.FragmentNavigator
import dev.enro.core.fragment.internal.AbstractSingleFragmentKey
import dev.enro.core.fragment.internal.SingleFragmentActivity
import dev.enro.core.internal.NoKeyNavigator

internal class InstructionParentInterceptor : NavigationInstructionInterceptor{

    override fun intercept(
        instruction: NavigationInstruction.Open,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): NavigationInstruction.Open {
        return instruction
            .setParentInstruction(parentContext, navigator)
            .setExecutorContext(parentContext)
            .setPreviouslyActiveId(parentContext)
    }

    private fun NavigationInstruction.Open.setParentInstruction(
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): NavigationInstruction.Open {
        if (internal.parentInstruction != null) return this

        fun findCorrectParentInstructionFor(instruction: NavigationInstruction.Open?): NavigationInstruction.Open? {
            if (navigator is FragmentNavigator) {
                return instruction
            }
            if (navigator is ComposableNavigator) {
                return instruction
            }

            if (instruction == null) return null
            val keyType = instruction.navigationKey::class
            val parentNavigator = parentContext.controller.navigatorForKeyType(keyType)
            if (parentNavigator is ActivityNavigator) return instruction
            if (parentNavigator is NoKeyNavigator) return instruction
            return findCorrectParentInstructionFor(instruction.internal.parentInstruction)
        }

        val parentInstruction = when (navigationDirection) {
            NavigationDirection.FORWARD -> findCorrectParentInstructionFor(parentContext.getNavigationHandleViewModel().instruction)
            NavigationDirection.REPLACE -> findCorrectParentInstructionFor(parentContext.getNavigationHandleViewModel().instruction)?.internal?.parentInstruction
            NavigationDirection.REPLACE_ROOT -> null
        }

        return internal.copy(parentInstruction = parentInstruction?.internal)
    }

    private fun NavigationInstruction.Open.setExecutorContext(
        parentContext: NavigationContext<*>
    ): NavigationInstruction.Open {
        if(parentContext.contextReference is SingleFragmentActivity) {
            val singleFragmentKey = parentContext.getNavigationHandle().asTyped<AbstractSingleFragmentKey>().key
            if(instructionId == singleFragmentKey.instruction.instructionId) {
                return internal
            }
        }
        return internal.copy(executorContext = parentContext.contextReference::class.java)
    }

    private fun NavigationInstruction.Open.setPreviouslyActiveId(
        parentContext: NavigationContext<*>
    ): NavigationInstruction.Open {
        if(internal.previouslyActiveId != null) return this
        return internal.copy(
            previouslyActiveId = parentContext.containerManager.activeContainer?.id
        )
    }
}