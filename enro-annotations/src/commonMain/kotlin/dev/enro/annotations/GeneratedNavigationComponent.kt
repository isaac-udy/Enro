package dev.enro.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class GeneratedNavigationComponent(
    val bindings: Array<KClass<out Any>>,
    val modules: Array<KClass<out Any>>
)