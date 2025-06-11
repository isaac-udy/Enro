package dev.enro.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public expect annotation class NavigationDestination(
    val key: KClass<out Any>
)