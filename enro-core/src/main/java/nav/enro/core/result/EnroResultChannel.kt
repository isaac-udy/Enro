package nav.enro.core.result

import nav.enro.core.NavigationKey

interface EnroResultChannel<T> {
    fun open(key: NavigationKey.WithResult<T>)
}