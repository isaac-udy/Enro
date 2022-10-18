package dev.enro.core

import android.os.Parcelable

public interface NavigationKey : Parcelable {
    public interface WithResult<T> : NavigationKey

    public interface SupportsPush : NavigationKey {
        public interface WithResult<T> : SupportsPush, NavigationKey.WithResult<T>
    }

    public interface SupportsPresent : NavigationKey {
        public interface WithResult<T> : SupportsPresent, NavigationKey.WithResult<T>
    }
}

/**
 * The EnroInternalNavigationKey interface is a marker interface that is present on all NavigationKeys
 * that are defined within the Enro library.
 *
 * There are several NavigationKey types which are used internally by Enro.
 * Often, these NavigationKeys are used to wrap NavigationInstructions/Keys to display them
 * in a different context.
 *
 * This is useful when you are generically inspecting a NavigationKey and would like to know whether
 * or not this is a NavigationKey unique to your codebase. For example, you may want to add an
 * EnroPlugin that logs "screen viewed" analytics for your screens when a NavigationHandle becomes active.
 * In these cases, you likely want to ignore NavigationHandles that have a NavigationKey that implements
 * InternalEnroNavigationKey.
 */
public interface EnroInternalNavigationKey