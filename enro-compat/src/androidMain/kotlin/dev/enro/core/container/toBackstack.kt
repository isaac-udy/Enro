package dev.enro.core.container

import dev.enro.NavigationBackstack
import dev.enro.NavigationKey

public fun List<NavigationKey.Instance<*>>.toBackstack(): NavigationBackstack = this