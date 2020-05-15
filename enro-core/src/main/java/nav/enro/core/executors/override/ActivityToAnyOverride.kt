package nav.enro.core.executors.override

import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext
import nav.enro.core.executors.ExecutorArgs
import nav.enro.core.executors.NavigationExecutor
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
    noinline launch: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit),
    noinline close: (NavigationContext<out Opens, out NavigationKey>) -> Unit
): NavigationExecutor<From, Opens, NavigationKey> =
    createOverride(From::class, Opens::class, launch, close)