package nav.enro.annotations

import kotlin.reflect.KClass
import java.lang.annotation.RetentionPolicy;

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class NavigationDestination(
    val key: KClass<out Any>,
    val allowDefault: Boolean = false
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