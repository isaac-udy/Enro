package dev.enro.core.controller.container

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.*
import dev.enro.core.activity.ActivityNavigator
import dev.enro.core.activity.DefaultActivityExecutor
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.compose.DefaultComposableExecutor
import dev.enro.core.fragment.DefaultFragmentExecutor
import dev.enro.core.fragment.FragmentNavigator
import dev.enro.core.synthetic.DefaultSyntheticExecutor
import dev.enro.core.synthetic.SyntheticDestination
import dev.enro.core.synthetic.SyntheticNavigator
import kotlin.reflect.KClass

internal class ExecutorContainer() {
    private val overrides: MutableMap<Pair<KClass<out Any>, KClass<out Any>>, NavigationExecutor<*,*,*>> = mutableMapOf()
    private val temporaryOverrides = mutableMapOf<Pair<KClass<out Any>, KClass<out Any>>, NavigationExecutor<*, *, *>>()

    fun addOverrides(executors: List<NavigationExecutor<*, *, *>>) {
        executors.forEach { navigationExecutor ->
            overrides[navigationExecutor.fromType to navigationExecutor.opensType] = navigationExecutor
        }
    }

    fun addTemporaryOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        temporaryOverrides[navigationExecutor.fromType to navigationExecutor.opensType] = navigationExecutor
    }

    fun removeTemporaryOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        temporaryOverrides.remove(navigationExecutor.fromType to navigationExecutor.opensType)
    }

    private fun overrideFor(types: Pair<KClass<out Any>, KClass<out Any>>): NavigationExecutor<out Any, out Any, out NavigationKey>? {
        return temporaryOverrides[types] ?: overrides[types]
    }

    internal fun executorForOpen(fromContext: NavigationContext<out Any>, navigator: Navigator<*, *>): OpenExecutorPair {
        val opensContext = navigator.contextType
        val opensContextIsActivity = navigator is ActivityNavigator
        val opensContextIsFragment = navigator is FragmentNavigator
        val opensContextIsComposable = navigator is ComposableNavigator
        val opensContextIsSynthetic = navigator is SyntheticNavigator

        fun getOverrideExecutor(overrideContext: NavigationContext<out Any>): OpenExecutorPair? {
            val override = overrideFor(overrideContext.contextReference::class to opensContext)
                ?: when (overrideContext.contextReference) {
                    is FragmentActivity -> overrideFor(FragmentActivity::class to opensContext)
                        ?: overrideFor(ComponentActivity::class to opensContext)
                    is ComponentActivity -> overrideFor(ComponentActivity::class to opensContext)
                    is Fragment -> overrideFor(Fragment::class to opensContext)
                    is ComposableDestination -> overrideFor(ComposableDestination::class to opensContext)
                    else -> null
                }
                ?: overrideFor(Any::class to opensContext)
                ?: when {
                    opensContextIsActivity -> overrideFor(overrideContext.contextReference::class to FragmentActivity::class)
                        ?: overrideFor(overrideContext.contextReference::class to ComponentActivity::class)
                    opensContextIsFragment -> overrideFor(overrideContext.contextReference::class to Fragment::class)
                    opensContextIsComposable -> overrideFor(overrideContext.contextReference::class to ComposableDestination::class)
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
            opensContextIsComposable -> OpenExecutorPair(fromContext, DefaultComposableExecutor)
            opensContextIsSynthetic -> OpenExecutorPair(fromContext, DefaultSyntheticExecutor)
            else -> throw EnroException.UnreachableState()
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun executorForClose(navigationContext: NavigationContext<out Any>): NavigationExecutor<Any, Any, NavigationKey> {
        val parentContextType = navigationContext.getNavigationHandleViewModel().instruction.internal.executorContext?.kotlin
        val contextType = navigationContext.contextReference::class

        val override = parentContextType?.let { parentContext ->
            val parentNavigator = navigationContext.controller.navigatorForContextType(parentContext)

            val parentContextIsActivity = parentNavigator is ActivityNavigator
            val parentContextIsFragment = parentNavigator is FragmentNavigator
            val parentContextIsComposable = parentNavigator is ComposableNavigator

            overrideFor(parentContext to contextType)
                ?: when  {
                    parentContextIsActivity -> overrideFor(FragmentActivity::class to contextType)
                        ?: overrideFor(ComponentActivity::class to contextType)
                    parentContextIsFragment -> overrideFor(Fragment::class to contextType)
                    parentContextIsComposable -> overrideFor(ComposableDestination::class to contextType)
                    else -> null
                }
                ?: overrideFor(Any::class to contextType)
                ?: when(navigationContext.contextReference) {
                    is FragmentActivity -> overrideFor(parentContext to FragmentActivity::class)
                        ?: overrideFor(parentContext to ComponentActivity::class)
                    is ComponentActivity -> overrideFor(parentContext to ComponentActivity::class)
                    is Fragment -> overrideFor(parentContext to Fragment::class)
                    is ComposableDestination -> overrideFor(parentContext to ComposableDestination::class)
                    else -> null
                }
                ?: overrideFor(parentContext to Any::class)
        } as? NavigationExecutor<Any, Any, NavigationKey>

        return override ?: when (navigationContext) {
            is ActivityContext -> DefaultActivityExecutor as NavigationExecutor<Any, Any, NavigationKey>
            is FragmentContext -> DefaultFragmentExecutor as NavigationExecutor<Any, Any, NavigationKey>
            is ComposeContext -> DefaultComposableExecutor as NavigationExecutor<Any, Any, NavigationKey>
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