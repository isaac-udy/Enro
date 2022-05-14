package dev.enro.core

import android.os.Parcelable

interface NavigationKey : Parcelable {
    interface WithResult<T> : NavigationKey

    interface SupportsForward : NavigationKey {
        interface WithResult<T> : SupportsForward, NavigationKey.WithResult<T>
    }

    interface SupportsPresent : NavigationKey {
        interface WithResult<T> : SupportsPresent, NavigationKey.WithResult<T>
    }
}