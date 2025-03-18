package dev.enro.core.controller

import kotlin.reflect.KClass

@PublishedApi
internal interface EnroDependencyRegistration {
    fun <T : Any> register(type: KClass<T>, createOnStart: Boolean, block: EnroDependencyScope.() -> T)
}

internal inline fun <reified T: Any> EnroDependencyRegistration.register(
    createOnStart: Boolean = false,
    noinline block: EnroDependencyScope.() -> T
) {
    register(T::class, createOnStart, block)
}