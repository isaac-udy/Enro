// Snippets for docs/ghpages/docs/advanced/animations.md
// A separate package, because a key can only have one destination.
@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.enro.docs.advanced.animations.provider

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetail(val id: Int) : NavigationKey

private fun thumbKey(id: Int) = "thumb-$id"

// region docs:animations/shared-elements-provider
@NavigationDestination(ItemDetail::class)
val itemDetailDestination = navigationDestination<ItemDetail> {
    Box(
        modifier = Modifier.sharedElement(
            rememberSharedContentState(key = thumbKey(navigation.key.id)),
            animatedVisibilityScope = this@navigationDestination,
        ),
    )
}
// endregion
