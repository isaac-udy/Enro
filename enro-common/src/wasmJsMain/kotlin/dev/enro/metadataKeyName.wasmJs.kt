package dev.enro

import kotlin.reflect.KClass

internal actual fun metadataKeyName(kClass: KClass<*>): String {
    return kClass.qualifiedName
        ?: kClass.simpleName
        ?: error("MetadataKey class must have a qualifiedName or simpleName")
}
