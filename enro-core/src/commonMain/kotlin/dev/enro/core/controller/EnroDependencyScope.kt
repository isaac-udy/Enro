package dev.enro.core.controller

import kotlin.reflect.KClass

public interface EnroDependencyScope {
    public val container: EnroDependencyContainer
}

@PublishedApi
internal inline fun <reified T: Any> EnroDependencyScope.get(): T {
    return container.get()
}

@PublishedApi
internal fun <T: Any> EnroDependencyScope.get(type: KClass<T>): T {
    return container.get(type)
}
