package dev.enro.core.controller.repository

import android.app.Activity
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationExecutor
import dev.enro.core.NavigationKey
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.container.DefaultContainerExecutor
import dev.enro.core.synthetic.DefaultSyntheticExecutor
import dev.enro.core.synthetic.SyntheticDestination
import kotlin.reflect.KClass

internal class ExecutorRepository(
    private val classHierarchyRepository: ClassHierarchyRepository
) {
    private val executors: MutableMap<Pair<KClass<out Any>, KClass<out Any>>, NavigationExecutor<*, *, *>> =
        mutableMapOf()
    private val overrides =
        mutableMapOf<Pair<KClass<out Any>, KClass<out Any>>, NavigationExecutor<*, *, *>>()

    init {
        executors[Any::class to Activity::class] = DefaultContainerExecutor
        executors[Any::class to Fragment::class] = DefaultContainerExecutor
        executors[Any::class to ComposableDestination::class] = DefaultContainerExecutor
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

    fun getExecutor(
        types: Pair<Class<out Any>, Class<out Any>>
    ): NavigationExecutor<Any, Any, NavigationKey>? {
        return overrides[types.first.kotlin to types.second.kotlin] as? NavigationExecutor<Any, Any, NavigationKey>
            ?: executors[types.first.kotlin to types.second.kotlin] as? NavigationExecutor<Any, Any, NavigationKey>
    }
}