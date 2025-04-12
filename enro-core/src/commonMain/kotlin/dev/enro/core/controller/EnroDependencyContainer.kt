package dev.enro.core.controller

import kotlin.reflect.KClass

public class EnroDependencyContainer internal constructor(
    internal val parentScope: EnroDependencyScope?,
    registration: EnroDependencyRegistration.() -> Unit,
) : EnroDependencyScope {

    override val container: EnroDependencyContainer = this

    @PublishedApi
    internal val bindings: MutableMap<KClass<*>, Dependency<*>> = mutableMapOf()

    init {
        registration.invoke(object : EnroDependencyRegistration {
            override fun <T : Any> register(
                type: KClass<T>,
                createOnStart: Boolean,
                block: EnroDependencyScope.() -> T
            ) {
                bindings[type] = Dependency(this@EnroDependencyContainer, block)
                    .also {
                        if(createOnStart) {
                            it.value
                        }
                    }
            }
        })
    }

    @PublishedApi
    internal fun <T: Any> get(type: KClass<T>): T {
        @Suppress("UNCHECKED_CAST") // We know that the type is correct
        return bindings[type]?.value as? T
            ?: parentScope?.get(type)
            ?: error("There is no dependency registered for type $type")
    }

    @PublishedApi
    internal inline fun <reified T: Any> get(): T {
        return get(T::class)
    }

    internal fun clear() {
        bindings.clear()
    }
}