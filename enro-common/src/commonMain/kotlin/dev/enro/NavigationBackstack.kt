package dev.enro

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Immutable
@Serializable
public class NavigationBackstack(
    private val backstack: List<NavigationKey.Instance<NavigationKey>>
): List<NavigationKey.Instance<NavigationKey>> by backstack {
    public val keys: List<NavigationKey> by lazy {
        map { it.key }
    }
}

public fun emptyBackstack(): NavigationBackstack {
    return NavigationBackstack(emptyList())
}

public fun backstackOf(vararg instance: NavigationKey.Instance<*>): NavigationBackstack {
    return NavigationBackstack(instance.toList())
}

public fun List<NavigationKey.Instance<*>>.asBackstack(): NavigationBackstack {
    return NavigationBackstack(this)
}

