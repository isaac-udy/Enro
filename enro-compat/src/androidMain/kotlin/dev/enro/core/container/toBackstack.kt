package dev.enro.core.container

import dev.enro.NavigationBackstack
import dev.enro.NavigationKey
import dev.enro.asBackstack

public fun List<NavigationKey.Instance<*>>.toBackstack(): NavigationBackstack = this.asBackstack()