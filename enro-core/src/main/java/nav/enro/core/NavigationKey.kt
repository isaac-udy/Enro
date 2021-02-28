package nav.enro.core

import android.os.Parcelable

interface NavigationKey : Parcelable {
    interface WithResult<T> : NavigationKey
}

interface AllowNested
interface AllowRoot