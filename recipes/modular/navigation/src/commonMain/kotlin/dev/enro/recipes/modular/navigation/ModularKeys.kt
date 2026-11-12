package dev.enro.recipes.modular.navigation

import dev.enro.NavigationKey
import kotlinx.serialization.Serializable

// The keys every feature module can open. A feature module opens another
// feature's screen with one of these keys, without depending on that feature.

/** Implemented in :recipes:modular:feature-home. */
@Serializable
data object HomeKey : NavigationKey

/** Implemented in :recipes:modular:feature-profile. */
@Serializable
data class ProfileKey(val userId: String) : NavigationKey

/** Implemented in :recipes:modular:feature-profile. */
@Serializable
data class ModularSettingsKey(val section: String = "general") : NavigationKey

/** Implemented in :recipes:modular:feature-profile. */
@Serializable
data object LoginKey : NavigationKey
