package dev.enro.recipes.tabs

import dev.enro.NavigationKey
import kotlinx.serialization.Serializable

/**
 * Root key for the Tab Navigation recipe. The destination implementation lives in
 * androidMain because it relies on enro-compat's NavigationContainerGroup which is
 * currently only available on Android.
 */
@Serializable
object TabsRecipe : NavigationKey
