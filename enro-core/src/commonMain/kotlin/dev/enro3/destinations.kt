package dev.enro3

import dev.enro3.ui.NavigationDestinationProvider
import kotlin.reflect.KClass

public var destinations: MutableMap<KClass<out NavigationKey>, NavigationDestinationProvider<out NavigationKey>> =
    mutableMapOf()

