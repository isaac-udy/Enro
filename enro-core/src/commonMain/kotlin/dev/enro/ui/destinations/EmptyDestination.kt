package dev.enro.ui.destinations

import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable

@Serializable
public data object EmptyNavigationKey : NavigationKey

public fun <T : NavigationKey> emptyDestination(): NavigationDestinationProvider<T> =
    navigationDestination {  }
