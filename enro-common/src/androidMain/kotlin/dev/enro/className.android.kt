package dev.enro

import dev.enro.annotations.AdvancedEnroApi
import kotlin.reflect.KClass

@AdvancedEnroApi
public actual fun stableClassName(kClass: KClass<*>): String {
    return kClass.qualifiedName
        ?: kClass.simpleName
        ?: error("$kClass has neither a qualifiedName nor a simpleName")
}

internal actual fun checkMetadataKeyNameIsUnique(namespace: KClass<*>, name: String, keyClass: KClass<*>) {}
