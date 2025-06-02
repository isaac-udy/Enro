package dev.enro3.ui.destinations

import dev.enro3.NavigationKey
import dev.enro3.ui.NavigationDestinationProvider
import dev.enro3.ui.navigationDestination
import kotlinx.serialization.Serializable

@Serializable
public data object EmptyNavigationKey : NavigationKey

public fun <T : NavigationKey> emptyDestination(): NavigationDestinationProvider<T> =
    navigationDestination {  }
