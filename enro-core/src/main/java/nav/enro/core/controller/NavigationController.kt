package nav.enro.core.controller

import android.R
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationDirection
import nav.enro.core.NavigationHandle
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.context.ActivityContext
import nav.enro.core.context.FragmentContext
import nav.enro.core.context.NavigationContext
import nav.enro.core.context.parentContext
import nav.enro.core.internal.SingleFragmentActivity
import nav.enro.core.internal.SingleFragmentKey
import nav.enro.core.executors.DefaultActivityExecutor
import nav.enro.core.executors.DefaultFragmentExecutor
import nav.enro.core.executors.ExecutorArgs
import nav.enro.core.executors.NavigationExecutor
import nav.enro.core.internal.handle.NavigationHandleActivityBinder
import nav.enro.core.internal.handle.NavigationHandleViewModel
import nav.enro.core.navigator.*
import kotlin.reflect.KClass


class NavigationController(
    navigators: List<NavigatorDefinition<*, *>>,
    overrides: List<NavigationExecutor<*, *, *>> = listOf(),
    private val plugins: List<EnroPlugin> = listOf()
) {
    internal var active: NavigationHandle<*>? = null
        set(value) {
            if(value == field) return
            field = value
            if(value != null) {
                plugins.forEach { it.onActive(value) }
            }
        }

    private val defaultNavigators = listOf(
        createActivityNavigator<SingleFragmentKey, SingleFragmentActivity> {
            acceptAllFragments(R.id.content)
        }
    )

    private val navigatorsByKeyType = (navigators + defaultNavigators)
        .map {
            it.navigator.keyType to it
        }
        .toMap()

    private val navigatorsByContextType = (navigators + defaultNavigators)
        .map {
            it.navigator.contextType to it
        }
        .toMap()

    private val overrides = (overrides + navigators.flatMap { it.executors })
        .map { (it.fromType to it.opensType) to it }.toMap()

    internal val handles = mutableMapOf<String, NavigationHandleViewModel<*>>()

    init {
        plugins.forEach { it.onAttached(this) }
    }

    internal fun open(
        navigationContext: NavigationContext<out Any, out NavigationKey>,
        instruction: NavigationInstruction.Open<*>
    ) {
        val navigator = navigatorForKeyType(instruction.navigationKey::class) ?: TODO()

        if (openOverrideFor(navigationContext, navigator, instruction)) return
        when (navigator) {
            is ActivityNavigator -> DefaultActivityExecutor.open(
                ExecutorArgs<Any, FragmentActivity, NavigationKey>(
                    navigationContext,
                    navigator,
                    instruction.setParentInstruction(navigationContext, navigator)
                )
            )
            is FragmentNavigator -> DefaultFragmentExecutor.open(
                ExecutorArgs<Any, Fragment, NavigationKey>(
                    navigationContext,
                    navigator,
                    instruction.setParentInstruction(navigationContext, navigator)
                )
            )
        }
    }

    internal fun close(
        navigationContext: NavigationContext<out Any, out NavigationKey>
    ) {
        if (!closeOverrideFor(navigationContext)) {
            when (navigationContext) {
                is ActivityContext -> DefaultActivityExecutor.close(navigationContext)
                is FragmentContext -> DefaultFragmentExecutor.close(navigationContext)
            }
        }
    }

    internal fun onOpened(navigationHandle: NavigationHandle<*>) {
        plugins.forEach { it.onOpened(navigationHandle) }
    }

    internal fun onClosed(navigationHandle: NavigationHandle<*>) {
        plugins.forEach { it.onClosed(navigationHandle) }
    }

    internal fun navigatorForContextType(
        contextType: KClass<*>
    ): Navigator<*, *>? {
        return navigatorsByContextType.getValue(contextType).navigator
    }

    internal fun navigatorForKeyType(
        keyType: KClass<out NavigationKey>
    ): Navigator<*, *>? {
        return navigatorsByKeyType.getValue(keyType).navigator
    }

    private fun openOverrideFor(
        fromContext: NavigationContext<out Any, *>,
        navigator: Navigator<out Any, out NavigationKey>,
        instruction: NavigationInstruction.Open<out NavigationKey>
    ): Boolean {
        @Suppress("UNCHECKED_CAST") // higher level logic dictates that this cast should succeed
        val override = overrides[fromContext.contextReference::class to navigator.contextType]
                as? NavigationExecutor<Any, Any, NavigationKey>

        if (override != null) {
            override.open(
                ExecutorArgs(
                    fromContext,
                    navigator,
                    instruction.setParentInstruction(fromContext, navigator)
                )
            )
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

    private fun closeOverrideFor(navigationContext: NavigationContext<out Any, out NavigationKey>): Boolean {
        val parentType = navigationContext.parentInstruction
            ?.let {
                navigatorForKeyType(it.navigationKey::class)
            }
            ?.contextType ?: return false

        @Suppress("UNCHECKED_CAST") // higher level logic dictates that this cast should succeed
        val override = overrides[parentType to navigationContext.navigator.contextType]
                as? NavigationExecutor<Any, Any, NavigationKey>
            ?: return false

        override.close(navigationContext)
        return true
    }

    private fun NavigationInstruction.Open<*>.setParentInstruction(
        parentContext: NavigationContext<*, *>,
        navigator: Navigator<out Any, out NavigationKey>
    ): NavigationInstruction.Open<*> {
        if (parentInstruction != null) return this

        fun findCorrectParentInstructionFor(instruction: NavigationInstruction.Open<*>?): NavigationInstruction.Open<*>? {
            if (navigator is FragmentNavigator) {
                return instruction
            }

            if (instruction == null) return null
            val keyType = instruction.navigationKey::class
            val parentNavigator = navigatorForKeyType(keyType)
            if (parentNavigator is ActivityNavigator) return instruction
            return findCorrectParentInstructionFor(instruction.parentInstruction)
        }

        val parentInstruction = when (navigationDirection) {
            NavigationDirection.FORWARD -> findCorrectParentInstructionFor(parentContext.instruction)
            NavigationDirection.REPLACE -> findCorrectParentInstructionFor(parentContext.instruction)?.parentInstruction
            NavigationDirection.REPLACE_ROOT -> null
        }

        return copy(parentInstruction = parentInstruction)
    }

    companion object {
        fun install(navigationApplication: NavigationApplication) {
            if (navigationApplication !is Application) TODO("Proper Exception")
            navigationApplication.registerActivityLifecycleCallbacks(
                NavigationHandleActivityBinder
            )
        }
    }
}