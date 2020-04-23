package nav.enro.core

import android.app.Application
import androidx.fragment.app.Fragment
import nav.enro.core.internal.SingleFragmentActivity
import nav.enro.core.internal.SingleFragmentKey
import nav.enro.core.internal.context.*
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.internal.executors.DefaultActivityExecutor
import nav.enro.core.internal.executors.DefaultFragmentExecutor
import nav.enro.core.internal.executors.NavigationExecutor
import nav.enro.core.internal.handle.NavigationHandleActivityBinder
import kotlin.reflect.KClass


class NavigationController(
    navigators: List<Navigator<*, *>>,
    overrides: List<NavigationExecutor<*, *, *>> = listOf()
) {
    private val defaultNavigators = listOf(
        activityNavigator<SingleFragmentKey, SingleFragmentActivity> {
            fragmentHost(android.R.id.content) { true }
        }
    )

    private val navigatorsByKeyType = (navigators + defaultNavigators)
        .map {
            it.keyType to it
        }
        .toMap()

    private val navigatorsByContextType = (navigators + defaultNavigators)
        .map {
            it.contextType to it
        }
        .toMap()

    private val overrides = overrides.map { (it.fromType to it.opensType) to it }.toMap()

    internal fun open(
        navigationContext: NavigationContext<out Any, out NavigationKey>,
        instruction: NavigationInstruction.Open<*>
    ) {
        val navigator = navigatorForKeyType(instruction.navigationKey::class) ?: TODO()

        if (openOverrideFor(navigationContext, navigator, instruction)) return
        when (navigator) {
            is ActivityNavigator -> DefaultActivityExecutor.open(
                navigationContext,
                navigator,
                instruction.setParentInstruction(navigationContext, navigator)
            )
            is FragmentNavigator -> DefaultFragmentExecutor.open(
                navigationContext,
                navigator,
                instruction.setParentInstruction(navigationContext, navigator)
            )
        }
    }

    internal fun close(
        navigationContext: NavigationContext<out Any, out NavigationKey>
    ) {
        if(closeOverrideFor(navigationContext)) return
        when (navigationContext) {
            is ActivityContext -> DefaultActivityExecutor.close(navigationContext)
            is FragmentContext -> DefaultFragmentExecutor.close(navigationContext)
        }
    }

    internal fun navigatorForContextType(
        contextType: KClass<*>
    ): Navigator<*, *>? {
        return navigatorsByContextType[contextType]
    }

    internal fun navigatorForKeyType(
        keyType: KClass<out NavigationKey>
    ): Navigator<*, *>? {
        return navigatorsByKeyType[keyType]
    }

    private fun openOverrideFor(
        fromContext: NavigationContext<out Any, *>,
        navigator: Navigator<out Any, out NavigationKey>,
        instruction: NavigationInstruction.Open<out NavigationKey>
    ): Boolean {
        val override = overrides[fromContext.contextReference::class to navigator.contextType]
                as? NavigationExecutor<Any, Any, NavigationKey>

        if(override != null) {
            override.open(fromContext, navigator, instruction.setParentInstruction(fromContext, navigator))
            return true
        }

        return when (fromContext.contextReference) {
            is Fragment -> openOverrideFor(
                fromContext.parentContext() ?: return false,
                navigator,
                instruction
            )
            else -> false
        }
    }

    private fun closeOverrideFor(navigationContext: NavigationContext<out Any, out NavigationKey>) : Boolean {
        val parentType = navigationContext.parentInstruction
            ?.let {
                navigatorForKeyType(it.navigationKey::class)
            }
            ?.contextType ?: return false

        val override = overrides[parentType to navigationContext.navigator.contextType]
                as? NavigationExecutor<Any, Any, NavigationKey>
            ?: return false

        override.close(navigationContext)
        return true
    }


    companion object {
        fun install(navigationApplication: NavigationApplication) {
            if (navigationApplication !is Application) TODO("Proper Exception")
            navigationApplication.registerActivityLifecycleCallbacks(
                NavigationHandleActivityBinder
            )
        }
    }

    private fun NavigationInstruction.Open<*>.setParentInstruction(
        parentContext: NavigationContext<*, *>,
        navigator: Navigator<out Any, out NavigationKey>
    ): NavigationInstruction.Open<*> {
        if(parentInstruction != null) return this

        fun findCorrectParentInstructionFor(instruction: NavigationInstruction.Open<*>?): NavigationInstruction.Open<*>? {
            if(navigator is FragmentNavigator) return instruction

            if (instruction == null) return null
            val keyType = instruction.navigationKey::class
            val parentNavigator = navigatorForKeyType(keyType)
            if (parentNavigator is ActivityNavigator) return instruction
            return findCorrectParentInstructionFor(instruction.parentInstruction)
        }

        val parentInstruction =  when (navigationDirection) {
            NavigationDirection.FORWARD -> findCorrectParentInstructionFor(parentContext.instruction)
            NavigationDirection.REPLACE -> findCorrectParentInstructionFor(parentContext.instruction)?.parentInstruction
            NavigationDirection.REPLACE_ROOT -> null
        }

        return copy(parentInstruction = parentInstruction)
    }
}