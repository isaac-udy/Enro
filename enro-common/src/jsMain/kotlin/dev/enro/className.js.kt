package dev.enro

import dev.enro.annotations.AdvancedEnroApi
import kotlin.reflect.KClass

@AdvancedEnroApi
public actual fun stableClassName(kClass: KClass<*>): String {
    return kClass.simpleName
        ?: error("$kClass has no simpleName, which Kotlin/JS needs to name it")
}
