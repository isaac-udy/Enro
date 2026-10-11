package dev.enro

import dev.enro.annotations.AdvancedEnroApi
import kotlin.reflect.KClass

@AdvancedEnroApi
public actual fun stableClassName(kClass: KClass<*>): String {
    return kClass.simpleName
        ?: error("$kClass has no simpleName, which Kotlin/JS needs to name it")
}

private val metadataKeyOwners = mutableMapOf<Pair<KClass<*>, String>, KClass<*>>()

internal actual fun checkMetadataKeyNameIsUnique(namespace: KClass<*>, name: String, keyClass: KClass<*>) {
    val owner = metadataKeyOwners.getOrPut(namespace to name) { keyClass }
    check(owner == keyClass) {
        "Two different ${namespace.simpleName} classes are both named \"$name\". Kotlin/JS only has " +
            "simple class names, so their values would overwrite each other. Give one of them an " +
            "explicit name, for example `${namespace.simpleName}<T>(default, name = \"com.example.$name\")`."
    }
}
