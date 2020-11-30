package nav.enro.core.controller

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.*
import nav.enro.core.activity.ActivityNavigator
import nav.enro.core.activity.DefaultActivityExecutor
import nav.enro.core.activity.NavigationHandleActivityBinder
import nav.enro.core.activity.createActivityNavigator
import nav.enro.core.fragment.DefaultFragmentExecutor
import nav.enro.core.fragment.FragmentNavigator
import nav.enro.core.fragment.NavigationHandleFragmentBinder
import nav.enro.core.fragment.internal.HiltSingleFragmentActivity
import nav.enro.core.fragment.internal.SingleFragmentActivity
import nav.enro.core.fragment.internal.SingleFragmentKey
import nav.enro.core.internal.NoKeyNavigator
import nav.enro.core.internal.NoNavigationKey
import nav.enro.core.internal.handle.NavigationHandleViewModel
import nav.enro.core.plugins.EnroHilt
import nav.enro.core.plugins.EnroPlugin
import nav.enro.core.synthetic.SyntheticDestination
import nav.enro.core.synthetic.SyntheticNavigator
import kotlin.reflect.KClass

// TODO split functionality out into more focused classes (e.g. OverrideController or similar)
// TODO has too many sideways dependencies
class NavigationController(
    navigators: List<Navigator<*, *>>,
    overrides: List<NavigationExecutor<*, *, *>> = listOf(),
    private val plugins: List<EnroPlugin> = listOf()
) {
    internal var active: NavigationHandle? = null
        set(value) {
            if(value == field) return
            field = value
            if(value != null) {
                if(value is NavigationHandleViewModel && !value.hasKey) {
                    field = null
                    return
                }
                plugins.forEach { it.onActive(value) }
            }
        }

    private val defaultNavigators = run {
        val useHilt = plugins.any { it is EnroHilt }
        val singleFragmentNavigator = if(useHilt) {
            createActivityNavigator<SingleFragmentKey, HiltSingleFragmentActivity>()
        }
        else {
            createActivityNavigator<SingleFragmentKey, SingleFragmentActivity>()
        }

        val noKeyProvidedNavigator = NoKeyNavigator()

        listOf(
            singleFragmentNavigator,
            noKeyProvidedNavigator
        )
    }

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
    private val temporaryOverrides = mutableMapOf<Pair<KClass<out Any>, KClass<out Any>>, NavigationExecutor<*, *, *>>()

    internal val handles = mutableMapOf<String, NavigationHandleViewModel>()

    init {
        plugins.forEach { it.onAttached(this) }
    }

    internal fun open(
        navigationContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Open
    ) {
        val navigator = navigatorForKeyType(instruction.navigationKey::class)
            ?: throw IllegalStateException("Attempted to execute $instruction but could not find a valid navigator for the key type on this instruction")

        if (openOverrideFor(navigationContext, navigator, instruction)) return
        when (navigator) {
            is ActivityNavigator -> DefaultActivityExecutor.open(
                ExecutorArgs(
                    navigationContext,
                    navigator,
                    instruction.navigationKey,
                    instruction.setParentInstruction(navigationContext, navigator)
                )
            )
            is FragmentNavigator -> DefaultFragmentExecutor.open(
                ExecutorArgs(
                    navigationContext,
                    navigator,
                    instruction.navigationKey,
                    instruction.setParentInstruction(navigationContext, navigator)
                )
            )
            is SyntheticNavigator -> (navigator.destination as SyntheticDestination<NavigationKey>)
                .process(navigationContext, instruction.navigationKey, instruction)

            is NoKeyNavigator -> { throw IllegalArgumentException() }
        }
    }

    internal fun close(
        navigationContext: NavigationContext<out Any>
    ) {
        if (!closeOverrideFor(navigationContext)) {
            when (navigationContext) {
                is ActivityContext -> DefaultActivityExecutor.close(navigationContext)
                is FragmentContext -> DefaultFragmentExecutor.close(navigationContext)
            }
        }
    }

    internal fun onOpened(navigationHandle: NavigationHandle) {
        plugins.forEach { it.onOpened(navigationHandle) }
    }

    internal fun onClosed(navigationHandle: NavigationHandle) {
        plugins.forEach { it.onClosed(navigationHandle) }
    }

    fun navigatorForContextType(
        contextType: KClass<*>
    ): Navigator<*, *>? {
        return navigatorsByContextType[contextType]
    }

    fun navigatorForKeyType(
        keyType: KClass<out NavigationKey>
    ): Navigator<*, *>? {
        return navigatorsByKeyType[keyType]
    }

    private fun overrideFor(types: Pair<KClass<out Any>, KClass<out Any>>): NavigationExecutor<out Any, out Any, out NavigationKey>? {
        return temporaryOverrides[types] ?: overrides[types]
    }

    private fun openOverrideFor(
        fromContext: NavigationContext<out Any>,
        navigator: Navigator<out NavigationKey, out Any>,
        instruction: NavigationInstruction.Open
    ): Boolean {

        val override = overrideFor(fromContext.contextReference::class to navigator.contextType)
            ?: when(fromContext.contextReference) {
                is FragmentActivity -> overrideFor(FragmentActivity::class to navigator.contextType)
                is Fragment -> overrideFor(Fragment::class to navigator.contextType)
                else -> null
            }
            ?: overrideFor(Any::class to navigator.contextType)
            ?: when(navigator) {
                is ActivityNavigator<*, *> -> overrideFor(fromContext.contextReference::class to FragmentActivity::class)
                is FragmentNavigator<*, *> -> overrideFor(fromContext.contextReference::class to Fragment::class)
                else -> null
            }
            ?: overrideFor(fromContext.contextReference::class to Any::class)

        if (override != null) {
            @Suppress("UNCHECKED_CAST") // higher level logic dictates that this cast should succeed
            override as NavigationExecutor<Any, Any, NavigationKey>
            override.open(
                ExecutorArgs(
                    fromContext,
                    navigator,
                    instruction.navigationKey,
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

    private fun closeOverrideFor(navigationContext: NavigationContext<out Any>): Boolean {
        val parentInstruction = navigationContext.getNavigationHandleViewModel().instruction.parentInstruction
        val parentNavigator = parentInstruction
            ?.let {
                if(it.navigationKey is SingleFragmentKey) {
                    it.parentInstruction
                } else it
            }
            ?.let {
                return@let navigatorForKeyType(it.navigationKey::class)
            }
            ?: return false

        val parentType = when(parentInstruction.navigationKey) {
            is NoNavigationKey -> parentInstruction.navigationKey.contextType.kotlin
            else -> parentNavigator.contextType
        }

        @Suppress("UNCHECKED_CAST")
        // higher level logic dictates that this cast should succeed
        val override = overrideFor(parentType to navigationContext.contextReference::class)
                as? NavigationExecutor<Any, Any, NavigationKey>
            ?: return false

        override.close(navigationContext)
        return true
    }

    private fun NavigationInstruction.Open.setParentInstruction(
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): NavigationInstruction.Open {
        if (parentInstruction != null) return this

        fun findCorrectParentInstructionFor(instruction: NavigationInstruction.Open?): NavigationInstruction.Open? {
            if (navigator is FragmentNavigator) {
                return instruction
            }

            if (instruction == null) return null
            val keyType = instruction.navigationKey::class
            val parentNavigator = navigatorForKeyType(keyType)
            if (parentNavigator is ActivityNavigator) return instruction
            if (parentNavigator is NoKeyNavigator) return instruction
            return findCorrectParentInstructionFor(instruction.parentInstruction)
        }

        val parentInstruction = when (navigationDirection) {
            NavigationDirection.FORWARD -> findCorrectParentInstructionFor(parentContext.getNavigationHandleViewModel().instruction)
            NavigationDirection.REPLACE -> findCorrectParentInstructionFor(parentContext.getNavigationHandleViewModel().instruction)?.parentInstruction
            NavigationDirection.REPLACE_ROOT -> null
        }

        return copy(parentInstruction = parentInstruction)
    }

    fun addOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        temporaryOverrides[navigationExecutor.fromType to navigationExecutor.opensType] = navigationExecutor
    }

    fun removeOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        temporaryOverrides.remove(navigationExecutor.fromType to navigationExecutor.opensType)
    }

    companion object {
        fun install(navigationApplication: NavigationApplication) {
            if (navigationApplication !is Application)
                throw IllegalArgumentException("A NavigationApplication must extend android.app.Application")

            navigationApplication.registerActivityLifecycleCallbacks(
                NavigationHandleActivityBinder
            )

            navigationApplication.registerActivityLifecycleCallbacks(
                NavigationHandleFragmentBinder
            )
        }
    }
}