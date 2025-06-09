package dev.enro.compat

import dev.enro.NavigationKey

public actual fun NavigationKey.Instance<NavigationKey>.isLegacyPresent(): Boolean {
    return false
}