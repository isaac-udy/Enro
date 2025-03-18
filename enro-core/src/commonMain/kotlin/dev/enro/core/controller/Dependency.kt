package dev.enro.core.controller

@PublishedApi
internal class Dependency<T: Any>(
    private val container: EnroDependencyContainer,
    private val createDependency: EnroDependencyScope.() -> T
) {
    private val lazy = lazy {
        container.createDependency()
    }
    val isInitialized get() = lazy.isInitialized()
    val value: T by lazy
}