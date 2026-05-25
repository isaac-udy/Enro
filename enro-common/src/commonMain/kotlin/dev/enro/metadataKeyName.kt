package dev.enro

import kotlin.reflect.KClass

/**
 * Stable string identifier for a [NavigationKey.MetadataKey]'s class.
 * Used as the storage key inside [NavigationKey.Metadata] and persisted
 * across saved-state round-trips, so the identifier must be:
 *
 * - **Stable across runs** — serialised metadata must restore to the
 *   same logical key on next launch.
 * - **Unique per MetadataKey class** — two different MetadataKey objects
 *   must not collide.
 *
 * On every target except Kotlin/JS this is `KClass.qualifiedName`. On
 * Kotlin/JS, `qualifiedName` is unsupported by the Kotlin reflection
 * API, so we fall back to `simpleName`. The JS fallback is correct for
 * the common `object MyKey : MetadataKey<...>` pattern as long as the
 * `MyKey` simple names are unique within the app.
 */
internal expect fun metadataKeyName(kClass: KClass<*>): String
