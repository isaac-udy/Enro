package nav.enro.annotations

import kotlin.reflect.KClass

annotation class NavigationDestination(
    val fromKey: KClass<out Any>
)