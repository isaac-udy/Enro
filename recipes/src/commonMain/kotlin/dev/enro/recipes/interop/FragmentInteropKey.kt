package dev.enro.recipes.interop

import dev.enro.NavigationKey
import kotlinx.serialization.Serializable

/**
 * Root key for the Fragment Interop recipe. The destination implementation lives in
 * androidMain because Fragment APIs are Android-only.
 */
@Serializable
object FragmentInteropRecipe : NavigationKey
