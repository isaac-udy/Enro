package dev.enro.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class NavigationDestination(
    val key: KClass<out Any>
)

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class NavigationComponent()

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class GeneratedNavigationBinding(
    val destination: String,
    val navigationKey: String
)

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class GeneratedNavigationModule(
    val bindings: Array<KClass<out Any>>,
)

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class GeneratedNavigationComponent(
    val bindings: Array<KClass<out Any>>,
    val modules: Array<KClass<out Any>>
)

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@Deprecated("This annotation is no longer required by Enro")
annotation class ExperimentalComposableDestination
