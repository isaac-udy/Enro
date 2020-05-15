package nav.enro.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class NavigationDestination(
    val key: KClass<out Any>,
    val allowDefault: Boolean = false
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class NavigationComponent()