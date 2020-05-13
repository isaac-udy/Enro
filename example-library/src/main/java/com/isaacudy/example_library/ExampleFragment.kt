package com.isaacudy.example_library

import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey

@Parcelize
data class ExampleOtherKey (val data: String): NavigationKey

@NavigationDestination(ExampleOtherKey::class)
class ExampleFragment  : Fragment()