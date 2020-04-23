package nav.enro.core.internal.executors.override

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.Navigator
import nav.enro.core.addOpenInstruction
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.context.activity
import nav.enro.core.internal.context.fragment
import nav.enro.core.internal.context.requireActivity
import nav.enro.core.internal.executors.DefaultActivityExecutor
import nav.enro.core.internal.executors.DefaultFragmentExecutor
import nav.enro.core.internal.executors.NavigationExecutor
import nav.enro.core.internal.executors.getParentFragment

inline fun <reified From : FragmentActivity, reified Opens : FragmentActivity> activityToActivityOverride(
    noinline launch: ((fromActivity: NavigationContext<out From, *>, instruction: NavigationInstruction.Open<*>, toIntent: Intent) -> Unit),
    noinline close: ((toActivity: Opens) -> Unit)
): NavigationExecutor<From, Opens, NavigationKey> =
    object : NavigationExecutor<From, Opens, NavigationKey>(
        fromType = From::class,
        opensType = Opens::class,
        keyType = NavigationKey::class
    ) {
        override fun open(
            fromContext: NavigationContext<out From, *>,
            navigator: Navigator<out Opens, out NavigationKey>,
            instruction: NavigationInstruction.Open<out NavigationKey>
        ) {
            val intent = Intent(fromContext.requireActivity(), navigator.contextType.java)
                .addOpenInstruction(instruction)
            launch(fromContext, instruction, intent)
        }

        override fun close(context: NavigationContext<out Opens, out NavigationKey>) {
            when (context.activity) {
                !is Opens -> DefaultActivityExecutor.close(context)
                else -> close(context.activity as Opens)
            }
        }
    }

inline fun <reified From : FragmentActivity, reified Opens : Fragment> activityToFragmentOverride(
    noinline launch: (fromActivity: NavigationContext<out From, *>, instruction: NavigationInstruction.Open<*>, toFragment: Opens) -> Unit,
    noinline close: (fromActivity: From, toFragment: Opens) -> Unit
): NavigationExecutor<From, Opens, NavigationKey> =
    object : NavigationExecutor<From, Opens, NavigationKey>(
        fromType = From::class,
        opensType = Opens::class,
        keyType = NavigationKey::class
    ) {
        override fun open(
            fromContext: NavigationContext<out From, *>,
            navigator: Navigator<out Opens, out NavigationKey>,
            instruction: NavigationInstruction.Open<out NavigationKey>
        ) {
            val fragment = DefaultFragmentExecutor.createFragment(fromContext.childFragmentManager, navigator, instruction)
            launch(fromContext, instruction, fragment as Opens)
        }

        override fun close(context: NavigationContext<out Opens, out NavigationKey>) {
            when {
                context.requireActivity() !is From -> DefaultFragmentExecutor.close(context)
                context.fragment !is Opens -> DefaultFragmentExecutor.close(context)
                else -> close.invoke(context.requireActivity() as From, context.fragment as Opens)
            }
        }
    }

inline fun <reified From : Fragment, reified Opens : Fragment> fragmentToFragmentOverride(
    noinline launch: (fromFragment: NavigationContext<out From, *>, instruction: NavigationInstruction.Open<*>, toFragment: Opens) -> Unit,
    noinline close: (fromFragment: From, toFragment: Opens) -> Unit
): NavigationExecutor<From, Opens, NavigationKey> =
    object : NavigationExecutor<From, Opens, NavigationKey>(
        fromType = From::class,
        opensType = Opens::class,
        keyType = NavigationKey::class
    ) {
        override fun open(
            fromContext: NavigationContext<out From, *>,
            navigator: Navigator<out Opens, out NavigationKey>,
            instruction: NavigationInstruction.Open<out NavigationKey>
        ) {
            val fragment = DefaultFragmentExecutor.createFragment(fromContext.childFragmentManager, navigator, instruction)
            launch(fromContext, instruction, fragment as Opens)
        }

        override fun close(context: NavigationContext<out Opens, out NavigationKey>) {
            val parent = context.getParentFragment()
            when {
                parent !is From -> DefaultFragmentExecutor.close(context)
                context.fragment !is Opens -> DefaultFragmentExecutor.close(context)
                else -> close(parent, context.fragment as Opens)
            }
        }
    }