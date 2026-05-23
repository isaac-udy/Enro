package dev.enro.controller.repository

import dev.enro.NavigationKey
import dev.enro.path.NavigationPathBinding
import dev.enro.path.ParsedPath


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
        if (matching.isEmpty()) return null
        if (matching.size == 1) return matching.single()

        val topScore = matching.maxOf { it.specificity }
        val mostSpecific = matching.filter { it.specificity == topScore }
        require(mostSpecific.size == 1) {
            "Multiple path bindings found for path: $path"
        }
        return mostSpecific.single()
    }

    public fun <T : NavigationKey> getPathBindingForKey(key: T): NavigationPathBinding<T>? {
        @Suppress("UNCHECKED_CAST")
        return bindings.firstOrNull { it.matches(key) } as NavigationPathBinding<T>?
    }

    public fun getPathBindingsForKey(key: NavigationKey): List<NavigationPathBinding<*>> {
        return bindings.filter { it.matches(key) }
    }
}