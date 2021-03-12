package dev.enro.core.controller.interceptor

import nav.enro.core.*
import nav.enro.core.activity.ActivityNavigator
import nav.enro.core.controller.container.NavigatorContainer
import nav.enro.core.fragment.FragmentNavigator
import nav.enro.core.fragment.internal.SingleFragmentActivity
import nav.enro.core.internal.NoKeyNavigator

internal class InstructionParentInterceptor(
    private val navigatorContainer: NavigatorContainer
) : NavigationInstructionInterceptor{

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

            if (instruction == null) return null
            val keyType = instruction.navigationKey::class
            val parentNavigator = navigatorContainer.navigatorForKeyType(keyType)
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
            return internal.copy(executorContext = parentContext.getNavigationHandleViewModel().instruction.internal.executorContext)
        }
        return internal.copy(executorContext = parentContext.contextReference::class.java)
    }

    private fun NavigationInstruction.Open.setPreviouslyActiveId(
        parentContext: NavigationContext<*>
    ): NavigationInstruction.Open {
        if(internal.previouslyActiveId != null) return this
        return internal.copy(
            previouslyActiveId = parentContext.childFragmentManager.primaryNavigationFragment?.getNavigationHandle()?.id
        )
    }
}