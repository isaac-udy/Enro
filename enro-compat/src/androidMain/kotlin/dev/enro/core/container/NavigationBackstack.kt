package dev.enro.core.container

import dev.enro.NavigationKey

@Deprecated("This function just returns the input list, NavigationBackstack is a type alias for the list type that is passed as a parameter, you should remove the function call and just reference the list directly.")
public fun NavigationBackstack(
    list: List<NavigationKey.Instance<NavigationKey>>
): dev.enro.NavigationBackstack {
    return list
}
