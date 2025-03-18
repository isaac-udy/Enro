package dev.enro.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class GeneratedNavigationModule(
    val bindings: Array<KClass<out Any>>,
)