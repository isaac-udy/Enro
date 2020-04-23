package nav.enro.core.internal.executors.override

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.internal.getAttributeResourceId
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.reflect.KClass

class NavigationExecutorOverride<From : Any, To : Any>(
    val fromType: KClass<From>,
    val toType: KClass<To>,

    private val launchActivity: ((From, NavigationInstruction.Open<*>, Intent) -> Unit)? = null,
    private val closeActivity: ((To) -> Unit)? = null,
    private val launchFragment: ((From, NavigationInstruction.Open<*>, To) -> Unit)? = null,
    private val closeFragment: ((From, To) -> Unit)? = null
) {

    fun launchActivity(from: From, instruction: NavigationInstruction.Open<*>, intent: Intent) {
        launchActivity?.invoke(from, instruction, intent)
            ?: throw IllegalArgumentException("${this::class.java.simpleName} cannot launch Activity")
    }

    fun closeActivity(activity: To) {
        closeActivity?.invoke(activity)
            ?: throw IllegalArgumentException("${this::class.java.simpleName} cannot close Activity")
    }

    fun launchFragment(from: From, instruction: NavigationInstruction.Open<*>, fragment: To) {
        launchFragment?.invoke(from, instruction, fragment)
            ?: throw IllegalArgumentException("${this::class.java.simpleName} cannot launch Fragment")
    }

    fun closeFragment(from: From, fragment: To) {
        closeFragment?.invoke(from, fragment)
            ?: throw IllegalArgumentException("${this::class.java.simpleName} cannot close Fragment")
    }
}

class PendingNavigationOverride(
    private val from: Any,
    private val override: NavigationExecutorOverride<*,*>
) {
    fun launchActivity(instruction: NavigationInstruction.Open<*>, intent: Intent) {
        (override as NavigationExecutorOverride<Any, Any>).launchActivity(from, instruction, intent)
    }

    fun launchFragment(instruction: NavigationInstruction.Open<*>, fragment: Fragment) {
        (override as NavigationExecutorOverride<Any, Any>).launchFragment(from, instruction, fragment)
    }
}

inline fun <reified From : FragmentActivity, reified To : FragmentActivity> activityToActivityOverride(
    noinline launch: ((fromActivity: From, instruction: NavigationInstruction.Open<*>, toIntent: Intent) -> Unit),
    noinline close: ((toActivity: To) -> Unit)
): NavigationExecutorOverride<From, To> =
    NavigationExecutorOverride(
        fromType = From::class,
        toType = To::class,
        launchActivity = launch,
        closeActivity = close
    )

inline fun <reified From : FragmentActivity, reified To : Fragment> activityToFragmentOverride(
    noinline launch: (fromActivity: From, instruction: NavigationInstruction.Open<*>, toFragment: To) -> Unit,
    noinline close: (fromActivity: From, toFragment: To) -> Unit
): NavigationExecutorOverride<From, To> = NavigationExecutorOverride(
    fromType = From::class,
    toType = To::class,
    launchFragment = launch,
    closeFragment = close
)

inline fun <reified From : Fragment, reified To : Fragment> fragmentToFragmentOverride(
    noinline launch: (fromFragment: From, instruction: NavigationInstruction.Open<*>, toFragment: To) -> Unit,
    noinline close: (fromFragment: From, toFragment: To) -> Unit
): NavigationExecutorOverride<From, To> = NavigationExecutorOverride(
    fromType = From::class,
    toType = To::class,
    launchFragment = launch,
    closeFragment = close
)