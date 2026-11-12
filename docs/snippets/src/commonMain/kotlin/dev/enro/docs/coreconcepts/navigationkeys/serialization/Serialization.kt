// Snippets for docs/ghpages/docs/core-concepts/navigation-keys.md
package dev.enro.docs.coreconcepts.navigationkeys.serialization

import dev.enro.NavigationKey
import kotlinx.serialization.Serializable

// region docs:navigation-keys/domain-type
@Serializable
data class UserId(val value: String)

@Serializable
data class ShowProfile(val userId: UserId) : NavigationKey
// endregion
