package nav.enro.core.executors.override

import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext
import nav.enro.core.executors.ExecutorArgs
import nav.enro.core.executors.NavigationExecutor
import kotlin.reflect.KClass

fun <From : FragmentActivity, Opens : FragmentActivity> createActivityToActivityOverride(
    fromClass: KClass<From>,
    opensClass: KClass<Opens>,
    launch: ((ExecutorArgs<From, Opens, NavigationKey>) -> Unit),
    close: ((context: NavigationContext<out Opens, out NavigationKey>) -> Unit)
): NavigationExecutor<From, Opens, NavigationKey> =
    object : NavigationExecutor<From, Opens, NavigationKey>(
        fromType = fromClass,
        opensType = opensClass,
        keyType = NavigationKey::class
    ) {
        override fun open(args: ExecutorArgs<From, Opens, NavigationKey>) {
            launch(args)
        }

        override fun close(context: NavigationContext<out Opens, out NavigationKey>) {
            close(context)
        }
    }


inline fun <reified From : FragmentActivity, reified Opens : FragmentActivity> createActivityToActivityOverride(
    noinline launch: ((ExecutorArgs<From, Opens, NavigationKey>) -> Unit),
    noinline close: ((context: NavigationContext<out Opens, out NavigationKey>) -> Unit)
): NavigationExecutor<From, Opens, NavigationKey> =
    createActivityToActivityOverride(From::class, Opens::class, launch, close)
