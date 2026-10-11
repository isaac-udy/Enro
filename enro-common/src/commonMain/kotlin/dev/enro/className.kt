package dev.enro

import dev.enro.annotations.AdvancedEnroApi
import kotlin.reflect.KClass

/**
 * A stable string identifier for a class, suitable for persisting in saved state (for example
 * as the storage key of a [NavigationKey.MetadataKey] inside [NavigationKey.Metadata]).
 *
 * On every target except Kotlin/JS this is `KClass.qualifiedName`, falling back to `simpleName`.
 * Kotlin/JS reflection has no `qualifiedName`, so there it is `simpleName`, which is only unique
 * while no two persisted classes in the app share a simple name.
 *
 * Throws for a class with neither name, such as an anonymous class.
 */
@AdvancedEnroApi
public expect fun stableClassName(kClass: KClass<*>): String

/**
 * The name a metadata key's values are stored under: [explicitName], or [stableClassName] of
 * [keyClass] when the key has no explicit name.
 *
 * [namespace] is the key type whose values share one map (for example
 * [NavigationKey.MetadataKey]). On Kotlin/JS, where [stableClassName] is a simple name, this
 * throws when a different key class in the same [namespace] already has the same name, because the
 * two keys would read and overwrite each other's values.
 */
@AdvancedEnroApi
public fun metadataKeyName(
    namespace: KClass<*>,
    keyClass: KClass<*>,
    explicitName: String?,
): String {
    val name = explicitName ?: stableClassName(keyClass)
    checkMetadataKeyNameIsUnique(namespace, name, keyClass)
    return name
}

internal expect fun checkMetadataKeyNameIsUnique(namespace: KClass<*>, name: String, keyClass: KClass<*>)
