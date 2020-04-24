package nav.enro.core.executors.override

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext
import nav.enro.core.context.fragment
import nav.enro.core.context.parentActivity
import nav.enro.core.executors.DefaultFragmentExecutor
import nav.enro.core.executors.ExecutorArgs
import nav.enro.core.executors.NavigationExecutor

inline fun <reified From : FragmentActivity, reified Opens : Fragment> createActivityToFragmentOverride(
    noinline launch: ((ExecutorArgs<From, Opens, NavigationKey>) -> Unit),
    noinline close: (NavigationContext<out Opens, out NavigationKey>) -> Unit
): NavigationExecutor<From, Opens, NavigationKey> =
    object : NavigationExecutor<From, Opens, NavigationKey>(
        fromType = From::class,
        opensType = Opens::class,
        keyType = NavigationKey::class
    ) {
        override fun open(args: ExecutorArgs<From, Opens, NavigationKey>) {
            launch(args)
        }

        override fun close(context: NavigationContext<out Opens, out NavigationKey>) {
            when {
                context.parentActivity !is From -> DefaultFragmentExecutor.close(context)
                context.fragment !is Opens -> DefaultFragmentExecutor.close(context)
                else -> close.invoke(context)
            }
        }
    }