package dev.enro.recipes.multiplestacks

import dev.enro.NavigationKey
import kotlinx.serialization.Serializable

/**
 * Root key for the Multiple Back Stacks recipe. The destination implementation lives in
 * androidMain because it relies on enro-compat's NavigationContainerGroup which is
 * currently only available on Android.
 */
@Serializable
object MultipleBackStacksRecipe : NavigationKey
