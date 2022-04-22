package dev.enro.core.controller.interceptor

import android.util.Log
import dev.enro.core.*
import dev.enro.core.activity.ActivityNavigator
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.controller.container.NavigatorContainer
import dev.enro.core.fragment.FragmentNavigator
import dev.enro.core.fragment.internal.AbstractSingleFragmentKey
import dev.enro.core.fragment.internal.SingleFragmentActivity
import dev.enro.core.internal.NoKeyNavigator

internal class ExecutorContextInterceptor : NavigationInstructionInterceptor{

    override fun intercept(
        instruction: NavigationInstruction.Open,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): NavigationInstruction.Open {
        return instruction
            .setExecutorContext(parentContext)
    }

    private fun NavigationInstruction.Open.setExecutorContext(
        parentContext: NavigationContext<*>
    ): NavigationInstruction.Open {
        // If the executor context has been set, don't change it
        if(internal.executorContext != null) return internal

        if(parentContext.contextReference is SingleFragmentActivity) {
            val singleFragmentKey = parentContext.getNavigationHandle().asTyped<AbstractSingleFragmentKey>().key
            if(instructionId == singleFragmentKey.instruction.instructionId) {
                return internal
            }
        }
        return internal.copy(executorContext = parentContext.contextReference::class.java)
    }
}