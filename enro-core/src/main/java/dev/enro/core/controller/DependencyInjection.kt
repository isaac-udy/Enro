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
        return bindings[type]?.value as? T
            ?: parentScope?.get(type)
            ?: throw NullPointerException()
    }

    @PublishedApi
    internal inline fun <reified T: Any> get(): T {
        return get(T::class)
    }

    internal fun clear() {
        bindings.clear()
    }
}