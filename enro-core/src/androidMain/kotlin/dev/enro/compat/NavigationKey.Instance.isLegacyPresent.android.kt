package dev.enro.compat

import dev.enro.NavigationKey
import dev.enro.core.NavigationDirection

public actual fun NavigationKey.Instance<NavigationKey>.isLegacyPresent(): Boolean {
    return metadata.get(NavigationDirection.MetadataKey) == NavigationDirection.Present
}