package dev.enro.tests.module

import android.os.Parcelable
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

@Parcelize
object ModuleOneDestination : Parcelable, NavigationKey.SupportsPush
