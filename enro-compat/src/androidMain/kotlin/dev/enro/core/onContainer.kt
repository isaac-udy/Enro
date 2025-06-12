package dev.enro.core

import dev.enro.NavigationHandle

@Deprecated("""
    onContainer actions are no longer supported. Instead of using onContainer, you should instead
    define a synthetic destination that performs the same action.
""", level = DeprecationLevel.ERROR)
public fun NavigationHandle<*>.onContainer() {
    error("")
}