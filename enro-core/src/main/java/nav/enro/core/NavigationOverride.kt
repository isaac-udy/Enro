package nav.enro.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.activity.DefaultActivityExecutor
import nav.enro.core.fragment.DefaultFragmentExecutor
import kotlin.reflect.KClass

fun <From : Any, Opens : Any> createOverride(
    fromClass: KClass<From>,
    opensClass: KClass<Opens>,
    open: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit),
    close: ((context: NavigationContext<out Opens>) -> Unit)
): NavigationExecutor<From, Opens, NavigationKey> =
    object : NavigationExecutor<From, Opens, NavigationKey>(
        fromType = fromClass,
        opensType = opensClass,
        keyType = NavigationKey::class
    ) {
        override fun open(args: ExecutorArgs<out From, out Opens, out NavigationKey>) {
            open(args)
        }

        override fun close(context: NavigationContext<out Opens>) {
            close(context)
        }
    }

inline fun <reified From : Any, reified Opens : Any> createOverride(
    noinline open: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit) = defaultOpen(),
    noinline close: (NavigationContext<out Opens>) -> Unit = defaultClose()
): NavigationExecutor<From, Opens, NavigationKey> =
    createOverride(From::class, Opens::class, open, close)

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