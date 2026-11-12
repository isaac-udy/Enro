// Snippets for docs/ghpages/docs/core-concepts/navigation-destinations.md
package dev.enro.docs.coreconcepts.navigationdestinations

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDestination as Destination // docs:hide
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope
import dev.enro.ui.destinations.syntheticDestination
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import dev.enro.ui.scenes.dialog
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data object ConfirmDialog : NavigationKey

@Serializable
data object InfoSheet : NavigationKey

@Serializable
data class Toast(val message: String) : NavigationKey

@Serializable
class LoginScreen : NavigationKey

private object sessionRepository {
    fun clearSession() {}
}

private class MyAdaptiveSceneStrategy : NavigationSceneStrategy {
    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<Destination<NavigationKey>>,
    ): NavigationScene? = null
}

// region docs:navigation-destinations/composable
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
// endregion

// region docs:navigation-destinations/provider
@NavigationDestination(ConfirmDialog::class)
val confirmDialogDestination: NavigationDestinationProvider<ConfirmDialog> = navigationDestination(
    metadata = {
        dialog(
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            )
        )
    },
) {
    AlertDialog(
        onDismissRequest = { navigation.close() },
        title = { Text("Confirm") },
        text = { Text("Are you sure?") },
        confirmButton = { TextButton(onClick = { navigation.close() }) { Text("Yes") } },
        dismissButton = { TextButton(onClick = { navigation.close() }) { Text("No") } },
    )
}
// endregion

// region docs:navigation-destinations/direct-overlay
@NavigationDestination(InfoSheet::class)
val infoSheetDestination = navigationDestination<InfoSheet>(
    metadata = { directOverlay() }
) {
    Surface { Text("Info") }
}
// endregion

// region docs:navigation-destinations/conditional-metadata
@Serializable
data class ItemDetail(val itemId: String, val showAsDialog: Boolean = false) : NavigationKey

@NavigationDestination(ItemDetail::class)
val itemDetailDestination = navigationDestination<ItemDetail>(
    metadata = {
        if (key.showAsDialog) dialog()
        // otherwise: rendered normally
    }
) {
    Column { Text("Item: ${navigation.key.itemId}") }
}
// endregion

// region docs:navigation-destinations/synthetic
@Serializable
object Logout : NavigationKey

@NavigationDestination(Logout::class)
val logout = syntheticDestination<Logout> {
    sessionRepository.clearSession()
    open(LoginScreen())
}
// endregion

private val defaultChain = // docs:hide
    // region docs:navigation-destinations/default-chain
    NavigationSceneStrategy.from(
        DialogSceneStrategy(),
        DirectOverlaySceneStrategy(),
        SinglePaneSceneStrategy(),
    )
    // endregion

@Composable
fun CustomChain(container: NavigationContainerState) {
    // region docs:navigation-destinations/custom-chain
    NavigationDisplay(
        state = container,
        sceneStrategy = NavigationSceneStrategy.from(
            MyAdaptiveSceneStrategy(),
            DialogSceneStrategy(),
            DirectOverlaySceneStrategy(),
            SinglePaneSceneStrategy(),
        ),
    )
    // endregion
}

// region docs:navigation-destinations/overlay-transitions
@NavigationDestination(Toast::class)
val toastDestination = navigationDestination<Toast>(
    metadata = {
        directOverlay(
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
        )
    },
) {
    Surface { Text(navigation.key.message) }
}
// endregion

// region docs:navigation-destinations/manual-registration
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        destination(confirmDialogDestination)
        destination(itemDetailDestination)
    }
)
// endregion
