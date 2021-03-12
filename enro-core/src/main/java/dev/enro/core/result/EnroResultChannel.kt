package dev.enro.core.result

import dev.enro.core.NavigationKey

interface EnroResultChannel<T> {
    fun open(key: NavigationKey.WithResult<T>)
}