package dev.enro.core.controller.container

import android.app.Activity
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationExecutor
import dev.enro.core.NavigationKey
import dev.enro.core.activity.DefaultActivityExecutor
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.DefaultComposableExecutor
import dev.enro.core.controller.reflection.ReflectionCache
import dev.enro.core.fragment.DefaultFragmentExecutor
import dev.enro.core.synthetic.DefaultSyntheticExecutor
import dev.enro.core.synthetic.SyntheticDestination
import kotlin.reflect.KClass

internal class ExecutorContainer {
    private val executors: MutableMap<Pair<KClass<out Any>, KClass<out Any>>, NavigationExecutor<*,*,*>> = mutableMapOf()
    private val overrides = mutableMapOf<Pair<KClass<out Any>, KClass<out Any>>, NavigationExecutor<*, *, *>>()


    init {
        executors[Any::class to Activity::class] = DefaultActivityExecutor
        executors[Any::class to Fragment::class] = DefaultFragmentExecutor
        executors[Any::class to ComposableDestination::class] = DefaultComposableExecutor
        executors[Any::class to SyntheticDestination::class] = DefaultSyntheticExecutor
    }

    fun addExecutors(executors: List<NavigationExecutor<*, *, *>>) {
        executors.forEach { navigationExecutor ->
            this.executors[navigationExecutor.fromType to navigationExecutor.opensType] = navigationExecutor
        }
    }

    fun addExecutorOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        overrides[navigationExecutor.fromType to navigationExecutor.opensType] = navigationExecutor
    }

    fun removeExecutorOverride(navigationExecutor: NavigationExecutor<*, *, *>) {
        overrides.remove(navigationExecutor.fromType to navigationExecutor.opensType)
    }

    fun executorFor(types: Pair<Class<out Any>, Class<out Any>>): NavigationExecutor<Any, Any, NavigationKey> {
        return ReflectionCache.getClassHierarchyPairs(types.first, types.second)
            .asSequence()
            .mapNotNull {
                overrides[it.first.kotlin to it.second.kotlin] as? NavigationExecutor<Any, Any, NavigationKey>
                    ?: executors[it.first.kotlin to it.second.kotlin] as? NavigationExecutor<Any, Any, NavigationKey>
            }
            .first()
    }
}