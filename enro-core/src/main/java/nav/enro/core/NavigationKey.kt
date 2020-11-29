package nav.enro.core

import android.os.Bundle
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

interface NavigationKey : Parcelable

@Parcelize
internal class NoNavigationKeyBound(
    val contextType: Class<*>,
    val arguments: Bundle?
) : NavigationKey