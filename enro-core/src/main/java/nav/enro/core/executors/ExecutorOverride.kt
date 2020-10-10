package nav.enro.core.executors

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext
import kotlin.reflect.KClass

fun <From : Any, Opens : Any> createOverride(
    fromClass: KClass<From>,
    opensClass: KClass<Opens>,
    launch: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit),
    close: ((context: NavigationContext<out Opens, out NavigationKey>) -> Unit)
): NavigationExecutor<From, Opens, NavigationKey> =
    object : NavigationExecutor<From, Opens, NavigationKey>(
        fromType = fromClass,
        opensType = opensClass,
        keyType = NavigationKey::class
    ) {
        override fun open(args: ExecutorArgs<out From, out Opens, out NavigationKey>) {
            launch(args)
        }

        override fun close(context: NavigationContext<out Opens, out NavigationKey>) {
            close(context)
        }
    }

inline fun <reified From : Any, reified Opens : Any> createOverride(
    noinline launch: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit) = defaultLaunch(),
    noinline close: (NavigationContext<out Opens, out NavigationKey>) -> Unit = defaultClose()
): NavigationExecutor<From, Opens, NavigationKey> =
    createOverride(From::class, Opens::class, launch, close)

@Suppress("UNCHECKED_CAST")
inline fun <reified Opens: Any> defaultLaunch(): ((ExecutorArgs<out Any, out Opens, out NavigationKey>) -> Unit) {
    return when {
        FragmentActivity::class.java.isAssignableFrom(Opens::class.java) ->
            DefaultActivityExecutor::open as ((ExecutorArgs<out Any, out Opens, out NavigationKey>) -> Unit)

        Fragment::class.java.isAssignableFrom(Opens::class.java) ->
            DefaultFragmentExecutor::open as ((ExecutorArgs<out Any, out Opens, out NavigationKey>) -> Unit)

        else -> throw IllegalArgumentException("No default launch executor found for ${Opens::class}")
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Opens: Any> defaultClose(): (NavigationContext<out Opens, out NavigationKey>) -> Unit {
    return when {
        FragmentActivity::class.java.isAssignableFrom(Opens::class.java) ->
            DefaultActivityExecutor::close as (NavigationContext<out Opens, out NavigationKey>) -> Unit

        Fragment::class.java.isAssignableFrom(Opens::class.java) ->
            DefaultFragmentExecutor::close as (NavigationContext<out Opens, out NavigationKey>) -> Unit

        else -> throw IllegalArgumentException("No default close executor found for ${Opens::class}")
    }
}