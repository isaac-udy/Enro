package dev.enro.core

import android.os.Parcelable

interface NavigationKey : Parcelable {
    interface WithResult<T> : NavigationKey

    interface SupportsPush : NavigationKey {
        interface WithResult<T> : SupportsPush, NavigationKey.WithResult<T>
    }

    interface SupportsPresent : NavigationKey {
        interface WithResult<T> : SupportsPresent, NavigationKey.WithResult<T>
    }
}