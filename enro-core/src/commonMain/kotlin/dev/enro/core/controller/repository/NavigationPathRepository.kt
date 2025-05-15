package dev.enro.core.controller.repository

import dev.enro.core.NavigationKey
import dev.enro.core.path.NavigationPathBinding
import dev.enro.core.path.ParsedPath

public class NavigationPathRepository {
    private val bindings = mutableListOf<NavigationPathBinding<*>>()

    public fun <T : NavigationKey> addPath(path: NavigationPathBinding<T>) {
        this.bindings.add(path)
    }

    public fun <T : NavigationKey> getPathBinding(): List<NavigationPathBinding<T>> {
        return bindings.filterIsInstance<NavigationPathBinding<T>>()
    }

    public fun getPathBinding(path: ParsedPath): NavigationPathBinding<*>? {
        val matching = bindings.filter { it.matches(path) }
        require(matching.isNotEmpty()) {
            "No path binding found for path: $path"
        }
        require(matching.size == 1) {
            "Multiple path bindings found for path: $path"
        }
        return matching.single()
    }
}