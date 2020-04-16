package nav.enro.core

import android.app.Application
import nav.enro.core.internal.SingleFragmentActivity
import nav.enro.core.internal.SingleFragmentKey
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.executors.ActivityNavigationExecutor
import nav.enro.core.internal.executors.FragmentNavigationExecutor
import nav.enro.core.internal.handle.NavigationHandleActivityBinder
import kotlin.reflect.KClass


class NavigationController(
    navigators: List<Navigator<*>>
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

    private val activityNavigationExecutor = ActivityNavigationExecutor()
    private val fragmentNavigationExecutor = FragmentNavigationExecutor()

    internal fun open(
        navigationContext: NavigationContext<out NavigationKey>,
        instruction: NavigationInstruction.Open
    ) {
        val navigator = navigatorFromKeyType(instruction.navigationKey::class) ?: TODO()
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
        navigationContext: NavigationContext<out NavigationKey>
    ) {
        when (navigationContext) {
            is ActivityContext -> activityNavigationExecutor.close(navigationContext)
            is FragmentContext -> fragmentNavigationExecutor.close(navigationContext)
        }
    }

    internal fun navigatorFromContextType(
        contextType: KClass<*>
    ): Navigator<*>? {
        return navigatorsByContextType[contextType]
    }

    internal fun navigatorFromKeyType(
        keyType: KClass<out NavigationKey>
    ): Navigator<*>? {
        return navigatorsByKeyType[keyType]
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