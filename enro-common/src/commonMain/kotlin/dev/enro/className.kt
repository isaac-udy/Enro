package dev.enro

import dev.enro.annotations.AdvancedEnroApi
import kotlin.reflect.KClass

/**
 * Returns a string identifier for [kClass] that is safe to store in saved state, for example
 * as the name of a [NavigationKey.MetadataKey] in [NavigationKey.Metadata].
 *
 * On every target except Kotlin/JS, the identifier is `KClass.qualifiedName`, or `simpleName`
 * when `qualifiedName` is null. Kotlin/JS reflection has no `qualifiedName`, so the identifier
 * on Kotlin/JS is `simpleName`, and two classes with the same simple name have the same
 * identifier.
 *
 * Throws for a class with neither name, such as an anonymous class.
 */
@AdvancedEnroApi
public expect fun stableClassName(kClass: KClass<*>): String

/**
 * Returns the name under which the values of a metadata key are stored: [explicitName], or
 * the [stableClassName] of [keyClass] when [explicitName] is null.
 *
 * [namespace] is the key type whose values share one map (for example
 * [NavigationKey.MetadataKey]). On Kotlin/JS, metadataKeyName throws when a different key
 * class in the same [namespace] already has the same name, because each of the two keys
 * would read and overwrite the values of the other key.
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
