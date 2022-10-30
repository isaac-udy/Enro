package dev.enro.core.internal.handle

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import dev.enro.core.NavigationContext
import dev.enro.core.activity

/**
 * In applications that contain both AndroidX Navigation and Enro, the back pressed behaviour
 * that is set by the NavigationHandleViewModel takes precedence over the back pressed behaviours that
 * are set by AndroidX Navigation.
 *
 * This method checks whether or not a given NavigationContext<*> is a part of the AndroidX Navigation,
 * by checking whether or not the parent fragment is a NavHostFragment. If we see that it is a NavHostFragment,
 * we'll disable the back pressed callback, repeat the activity.onBackPressed, and then return true
 *
 * If we decide that the NavigationContext<*> does **not** belong to AndroidX Navigation, and
 * is either part of Enro, or not part of any navigation framework, then we return false, to indicate that no
 * action was performed.
 */
internal fun interceptBackPressForAndroidxNavigation(
    backPressedCallback: OnBackPressedCallback,
    context: NavigationContext<*>,
): Boolean {
    val fragment = context.contextReference as? Fragment ?: return false
    if (!isAndroidxNavigationOnTheClasspath) return false

    val parent = fragment.parentFragment
    if (parent is NavHostFragment) {
        backPressedCallback.isEnabled = false
        context.activity.onBackPressed()
        backPressedCallback.isEnabled = true
        return true
    }
    return false
}

private val isAndroidxNavigationOnTheClasspath by lazy {
    runCatching { NavHostFragment::class.java }
        .map { true }
        .getOrDefault(false)
}
