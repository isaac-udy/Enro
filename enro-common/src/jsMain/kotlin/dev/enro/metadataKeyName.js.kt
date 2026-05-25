package dev.enro

import kotlin.reflect.KClass

// Kotlin/JS reflection does not expose qualifiedName. simpleName is the
// only stable identifier available — collisions across packages are
// possible in principle, but the recommended `object MyKey : MetadataKey<…>`
// pattern produces a unique simpleName per definition site.
internal actual fun metadataKeyName(kClass: KClass<*>): String {
    return kClass.simpleName
        ?: error("MetadataKey class must have a simpleName on Kotlin/JS")
}
