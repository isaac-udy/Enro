package dev.enro.core.controller.container

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.*
import dev.enro.core.activity.DefaultActivityExecutor
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.DefaultComposableExecutor
import dev.enro.core.fragment.DefaultFragmentExecutor
import dev.enro.core.synthetic.DefaultSyntheticExecutor
import dev.enro.core.synthetic.SyntheticDestination
import kotlin.reflect.KClass

internal class ExecutorContainer(
    overrides: List<NavigationExecutor<*, *, *>>
) {
    private val overrides = overrides.map { (it.fromType to it.opensType) to it }.toMap()
    private val temporaryOverrides = mutableMapOf<Pair<KClass<out Any>, KClass<out Any>>, NavigationExecutor<*, *, *>>()

    fun addOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        temporaryOverrides[navigationExecutor.fromType to navigationExecutor.opensType] = navigationExecutor
    }

    fun removeOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        temporaryOverrides.remove(navigationExecutor.fromType to navigationExecutor.opensType)
    }

    private fun overrideFor(types: Pair<KClass<out Any>, KClass<out Any>>): NavigationExecutor<out Any, out Any, out NavigationKey>? {
        return temporaryOverrides[types] ?: overrides[types]
    }

    // TODO - Does not properly support Composable overrides as yet
    internal fun executorForOpen(fromContext: NavigationContext<out Any>, navigator: Navigator<*, *>): OpenExecutorPair {
        val opensContext = navigator.contextType
        val opensContextIsActivity by lazy {
            FragmentActivity::class.java.isAssignableFrom(opensContext.java)
        }

        val opensContextIsFragment by lazy {
            Fragment::class.java.isAssignableFrom(opensContext.java)
        }

        val opensContextIsComposable by lazy {
            ComposableDestination::class.java.isAssignableFrom(opensContext.java)
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
            opensContextIsComposable -> OpenExecutorPair(fromContext, DefaultComposableExecutor)
            opensContextIsSynthetic -> OpenExecutorPair(fromContext, DefaultSyntheticExecutor)
            else -> throw IllegalStateException()
        }
    }

    @Suppress("UNCHECKED_CAST")
    // TODO - Does not properly support Composable overrides as yet
    internal fun executorForClose(navigationContext: NavigationContext<out Any>): NavigationExecutor<Any, Any, NavigationKey> {
        val parentContextType = navigationContext.getNavigationHandleViewModel().instruction.internal.executorContext?.kotlin
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
                    is ComposeContext -> null
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