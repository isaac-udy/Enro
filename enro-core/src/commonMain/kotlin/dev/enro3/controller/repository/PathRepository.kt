package dev.enro3.controller.repository

import dev.enro3.NavigationKey
import dev.enro3.path.NavigationPathBinding
import dev.enro3.path.ParsedPath


public class PathRepository {
    private val bindings = mutableListOf<NavigationPathBinding<*>>()

    public fun addPaths(paths: List<NavigationPathBinding<*>>) {
        this.bindings.addAll(paths)
    }

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