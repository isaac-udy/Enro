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
