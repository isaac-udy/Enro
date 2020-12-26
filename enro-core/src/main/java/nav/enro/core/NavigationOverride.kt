package nav.enro.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.activity.DefaultActivityExecutor
import nav.enro.core.fragment.DefaultFragmentExecutor
import kotlin.reflect.KClass

fun <From : Any, Opens : Any> createOverride(
    fromClass: KClass<From>,
    opensClass: KClass<Opens>,
    block: NavigationExecutorBuilder<From, Opens, NavigationKey>.() -> Unit
): NavigationExecutor<From, Opens, NavigationKey> =
    NavigationExecutorBuilder(fromClass, opensClass, NavigationKey::class)
        .apply(block)
        .build()

inline fun <reified From : Any, reified Opens : Any> createOverride(
    noinline block: NavigationExecutorBuilder<From, Opens, NavigationKey>.() -> Unit
): NavigationExecutor<From, Opens, NavigationKey> =
    createOverride(From::class, Opens::class, block)

@Suppress("UNCHECKED_CAST")
inline fun <reified Opens: Any> defaultOpen(): ((ExecutorArgs<out Any, out Opens, out NavigationKey>) -> Unit) {
    return when {
        FragmentActivity::class.java.isAssignableFrom(Opens::class.java) ->
            DefaultActivityExecutor::open as ((ExecutorArgs<out Any, out Opens, out NavigationKey>) -> Unit)

        Fragment::class.java.isAssignableFrom(Opens::class.java) ->
            DefaultFragmentExecutor::open as ((ExecutorArgs<out Any, out Opens, out NavigationKey>) -> Unit)

        else -> throw IllegalArgumentException("No default launch executor found for ${Opens::class}")
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Opens: Any> defaultClose(): (NavigationContext<out Opens>) -> Unit {
    return when {
        FragmentActivity::class.java.isAssignableFrom(Opens::class.java) ->
            DefaultActivityExecutor::close as (NavigationContext<out Opens>) -> Unit

        Fragment::class.java.isAssignableFrom(Opens::class.java) ->
            DefaultFragmentExecutor::close as (NavigationContext<out Opens>) -> Unit

        else -> throw IllegalArgumentException("No default close executor found for ${Opens::class}")
    }
}