package dev.enro.core


/**
 * This function is used to set that a navigation key should use the original navigation binding
 * when navigating to a destination, in the case where a platform-specific implementation of the
 * navigation binding exists.
 *
 * This is useful for cases where a platform override for the navigation binding may want to
 * create a new destination that hosts or otherwise wraps the original navigation binding.
 *
 * For example, a platform override for the desktop may want to create a new window for a
 * specific navigation destination, and put the original navigation binding in a container
 * within that window.
 */
public fun <T: NavigationKey> T.useOriginalBinding(): NavigationKey.WithExtras<T> {
    return NavigationKey.WithExtras(
        navigationKey = this,
        extras = NavigationBinding.setExtrasToUseOriginalBinding(NavigationInstructionExtras())
    )
}

/**
 * See [NavigationKey.useOriginalBinding]
 */
public fun <T: NavigationKey> NavigationKey.WithExtras<T>.useOriginalBinding(): NavigationKey.WithExtras<T> {
    return NavigationKey.WithExtras(
        navigationKey = navigationKey,
        extras = NavigationBinding.setExtrasToUseOriginalBinding(extras)
    )
}