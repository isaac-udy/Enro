package nav.enro.core

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.internal.SingleFragmentActivity
import nav.enro.core.internal.SingleFragmentKey
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.executors.ActivityNavigationExecutor
import nav.enro.core.internal.executors.FragmentNavigationExecutor
import nav.enro.core.internal.executors.override.NavigationExecutorOverride
import nav.enro.core.internal.executors.override.PendingNavigationOverride
import nav.enro.core.internal.handle.NavigationHandleActivityBinder
import kotlin.reflect.KClass


class NavigationController(
    navigators: List<Navigator<*>>,
    overrides: List<NavigationExecutorOverride<*, *>> = listOf()
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

    private val overrides = overrides.map { (it.fromType to it.toType) to it }.toMap()

    private val activityNavigationExecutor = ActivityNavigationExecutor()
    private val fragmentNavigationExecutor = FragmentNavigationExecutor()

    internal fun open(
        navigationContext: NavigationContext<out Any, out NavigationKey>,
        instruction: NavigationInstruction.Open<*>
    ) {
        val navigator = navigatorForKeyType(instruction.navigationKey::class) ?: TODO()
        when (navigator) {
            is ActivityNavigator -> activityNavigationExecutor.open(
                navigator,
                navigationContext,
                instruction
            )
            is FragmentNavigator -> fragmentNavigationExecutor.open(
                navigator,
                navigationContext,
                instruction
            )
        }
    }

    internal fun close(
        navigationContext: NavigationContext<out Any, out NavigationKey>
    ) {
        when (navigationContext) {
            is ActivityContext -> activityNavigationExecutor.close(navigationContext)
            is FragmentContext -> fragmentNavigationExecutor.close(navigationContext)
        }
    }

    internal fun navigatorForContextType(
        contextType: KClass<*>
    ): Navigator<*>? {
        return navigatorsByContextType[contextType]
    }

    internal fun navigatorForKeyType(
        keyType: KClass<out NavigationKey>
    ): Navigator<*>? {
        return navigatorsByKeyType[keyType]
    }

    internal fun pendingOverrideFor(
        from: Any,
        toType: KClass<*>?
    ): PendingNavigationOverride? {
        val override = overrides[from::class to toType]
            ?: return when(from) {
                is FragmentActivity -> null
                is Fragment -> pendingOverrideFor(from.parentFragment ?: from.requireActivity(), toType)
                else -> TODO("Real error")
            }
        return PendingNavigationOverride(from, override)
    }

    internal fun overrideFor(
        fromType: KClass<*>?,
        toType: KClass<*>?
    ): NavigationExecutorOverride<Any, Any>? {
        return overrides[fromType to toType] as? NavigationExecutorOverride<Any, Any>
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