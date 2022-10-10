package dev.enro.core.fragment

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import dev.enro.core.NavigationContext
import dev.enro.core.fragment

/**
 * In applications that contain both AndroidX Navigation and Enro, the back pressed behaviour
 * that is set by the NavigationHandleViewModel takes precedence over the back pressed behaviours that
 * are set by AndroidX Navigation.
 *
 * This method checks whether or not a given NavigationContext<out Fragment> is a part of the AndroidX Navigation,
 * by checking whether or not the parent fragment is a NavHostFragment. If we see that it is a NavHostFragment,
 * we'll execute popBackStack (which is the same behaviour the back pressed behaviour set by AndroidX Navigation),
 * and then return true.
 *
 * If we decide that the NavigationContext<out Fragment> does **not** belong to AndroidX Navigation, and
 * is either part of Enro, or not part of any navigation framework, then we return false, to indicate that no
 * action was performed.
 */
internal fun interceptCloseInstructionForAndroidxNavigation(context: NavigationContext<out Fragment>): Boolean {
    if (!isAndroidxNavigationOnTheClasspath) return false
    val parent = context.fragment.parentFragment
    if (parent is NavHostFragment) {
        parent.navController.popBackStack()
        return true
    }
    return false
}

private val isAndroidxNavigationOnTheClasspath by lazy {
    runCatching { NavHostFragment::class.java }
        .map { true }
        .getOrDefault(false)
}
