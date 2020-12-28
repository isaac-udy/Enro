package nav.enro.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
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