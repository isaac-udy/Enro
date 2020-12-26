package nav.enro.core.controller

import android.app.Application
import android.os.Bundle
import android.util.Log
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
import nav.enro.core.synthetic.DefaultSyntheticExecutor
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

        val executor = executorForOpen(navigationContext, instruction)

        val args = ExecutorArgs(
            executor.context,
            navigator,
            instruction.navigationKey,
            instruction
                .setParentInstruction(executor.context, navigator)
                .setParentContext(executor.context)
        )

        executor.executor.preOpened(executor.context)
        executor.executor.open(args)
    }

    internal fun close(
        navigationContext: NavigationContext<out Any>
    ) {
        val executor = executorForClose(navigationContext)
        executor.preClosed(navigationContext)
        executor.close(navigationContext)
    }

    internal fun onOpened(navigationHandle: NavigationHandle) {
        plugins.forEach { it.onOpened(navigationHandle) }
    }

    internal fun onClosed(navigationHandle: NavigationHandle) {
        plugins.forEach { it.onClosed(navigationHandle) }
    }

    internal fun onContextCreated(navigationContext: NavigationContext<out Any>, savedInstanceState: Bundle?) {
        if(savedInstanceState == null) {
            executorForClose(navigationContext).postOpened(navigationContext)
        }
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

    internal fun executorForOpen(fromContext: NavigationContext<out Any>, instruction: NavigationInstruction.Open): OpenExecutorPair {
        val navigator = navigatorForKeyType(instruction.navigationKey::class)
            ?: throw IllegalStateException("Attempted to find executor for $instruction but could not find a valid navigator for the key type on this instruction")

        val opensContext = navigator.contextType
        val opensContextIsActivity by lazy {
            FragmentActivity::class.java.isAssignableFrom(opensContext.java)
        }

        val opensContextIsFragment by lazy {
            Fragment::class.java.isAssignableFrom(opensContext.java)
        }

        val opensContextIsSynthetic by lazy {
            SyntheticDestination::class.java.isAssignableFrom(opensContext.java)
        }

        fun getOverrideExecutor(overrideContext: NavigationContext<out Any>): OpenExecutorPair? {
            val override = overrideFor(overrideContext.contextReference::class to opensContext)
                ?: when (overrideContext.contextReference) {
                    is FragmentActivity -> overrideFor(FragmentActivity::class to opensContext)
                    is Fragment -> overrideFor(Fragment::class to opensContext)
                    else -> null
                }
                ?: overrideFor(Any::class to opensContext)
                ?: when {
                    opensContextIsActivity -> overrideFor(overrideContext.contextReference::class to FragmentActivity::class)
                    opensContextIsFragment -> overrideFor(overrideContext.contextReference::class to Fragment::class)
                    else -> null
                }
                ?: overrideFor(overrideContext.contextReference::class to Any::class)

            val parentContext = overrideContext.parentContext()
            return when {
                override != null -> OpenExecutorPair(overrideContext, override)
                parentContext != null -> getOverrideExecutor(parentContext)
                else -> null
            }
        }

        val override = getOverrideExecutor(fromContext)
        return override ?: when {
                opensContextIsActivity -> OpenExecutorPair(fromContext, DefaultActivityExecutor)
                opensContextIsFragment -> OpenExecutorPair(fromContext, DefaultFragmentExecutor)
                opensContextIsSynthetic -> OpenExecutorPair(fromContext, DefaultSyntheticExecutor)
                else -> throw IllegalStateException()
            }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun executorForClose(navigationContext: NavigationContext<out Any>): NavigationExecutor<Any, Any, NavigationKey> {
        val parentContextType = navigationContext.getNavigationHandleViewModel().instruction.parentContext?.kotlin
        val contextType = navigationContext.contextReference::class

        val override = parentContextType?.let { parentContext ->
            val parentContextIsActivity by lazy {
                FragmentActivity::class.java.isAssignableFrom(parentContext.java)
            }

            val parentContextIsFragment by lazy {
                Fragment::class.java.isAssignableFrom(parentContext.java)
            }

            overrideFor(parentContext to contextType)
                ?: when  {
                    parentContextIsActivity -> overrideFor(FragmentActivity::class to contextType)
                    parentContextIsFragment -> overrideFor(Fragment::class to contextType)
                    else -> null
                }
                ?: overrideFor(Any::class to contextType)
                ?: when(navigationContext) {
                    is ActivityContext -> overrideFor(parentContext to FragmentActivity::class)
                    is FragmentContext -> overrideFor(parentContext to Fragment::class)
                }
                ?: overrideFor(parentContext to Any::class)
        } as? NavigationExecutor<Any, Any, NavigationKey>

        return override ?: when (navigationContext) {
            is ActivityContext -> DefaultActivityExecutor as NavigationExecutor<Any, Any, NavigationKey>
            is FragmentContext -> DefaultFragmentExecutor as NavigationExecutor<Any, Any, NavigationKey>
        }
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

    private fun NavigationInstruction.Open.setParentContext(
        parentContext: NavigationContext<*>
    ): NavigationInstruction.Open {
        if(parentContext.contextReference is SingleFragmentActivity) {
            return copy(parentContext = parentContext.getNavigationHandleViewModel().instruction.parentContext)
        }
        return copy(parentContext = parentContext.contextReference::class.java)
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

@Suppress("UNCHECKED_CAST")
class OpenExecutorPair(
    context: NavigationContext<out Any>,
    executor: NavigationExecutor<out Any, out Any, out NavigationKey>
) {
    val context = context as NavigationContext<Any>
    val executor = executor as NavigationExecutor<Any, Any, NavigationKey>
}