/**
 * Enro Recipe: UIViewController (iOS)
 *
 * Demonstrates a `UIViewController` as a destination, shown three ways.
 *
 * `uiViewControllerDestination` binds a key to a function that creates a
 * UIViewController. Its `metadata` block returns flags that say how the
 * controller may be shown, in order of preference:
 *  - `SupportsCompose`: embedded in the Enro container, like a Composable
 *    destination
 *  - `SupportsPresent`: presented modally over the current screen
 *  - `SupportsUINavigationView`: pushed onto the `UINavigationController`
 *    that hosts the app's root view controller, when there is one
 *
 * Callers open all three the same way, with `navigation.open(key)`.
 *
 * The view controller can be plain UIKit, as [uiKitLabelDestination] is, or
 * an `EnroUIViewController` with Compose content, which can use
 * `navigationHandle()` to close itself.
 */
// Reading a UIView's bounds from Kotlin uses the C interop API.
@file:OptIn(ExperimentalForeignApi::class)

package dev.enro.recipes.interop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asBackstack
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.platform.EnroUIViewController
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.destinations.UIViewControllerDestination
import dev.enro.ui.destinations.uiViewControllerDestination
import dev.enro.ui.rememberNavigationContainer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController

@Serializable
object UIViewControllerRecipe : NavigationKey

@Serializable
data object UIViewControllerMenu : NavigationKey

@Serializable
data class UIKitLabel(val text: String) : NavigationKey

@Serializable
data object PresentedViewController : NavigationKey

@Serializable
data object PushedViewController : NavigationKey

@Composable
@NavigationDestination(UIViewControllerRecipe::class)
fun UIViewControllerRecipeScreen() {
    val navigation = navigationHandle<UIViewControllerRecipe>()
    RecipeScaffold(
        title = "UIViewController",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(UIViewControllerMenu.asInstance()),
        )
        Column(modifier = modifier) {
            // The embedded UIKit controller has no button of its own to go
            // back with, so the host offers one.
            if (container.backstack.size > 1) {
                Button(
                    onClick = { container.updateBackstack { it.take(1).asBackstack() } },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text("Back to menu")
                }
            }
            NavigationDisplay(
                state = container,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
@NavigationDestination(UIViewControllerMenu::class)
fun UIViewControllerMenuDestination() {
    val navigation = navigationHandle<UIViewControllerMenu>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("UIViewController destinations", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.open(UIKitLabel("A UILabel in a UIViewController")) }) {
            Text("Embed in this container")
        }
        Button(onClick = { navigation.open(PresentedViewController) }) {
            Text("Present modally")
        }
        Button(onClick = { navigation.open(PushedViewController) }) {
            Text("Push onto the UINavigationController")
        }
    }
}

// region docs:ios/view-controller-destinations
// Plain UIKit, embedded in the Enro container.
@NavigationDestination(UIKitLabel::class)
val uiKitLabelDestination = uiViewControllerDestination<UIKitLabel>(
    metadata = { listOf(UIViewControllerDestination.SupportsCompose) },
    viewController = { instance ->
        UIViewController().apply {
            view.backgroundColor = UIColor.whiteColor
            val label = UILabel().apply {
                text = instance.key.text
                textAlignment = NSTextAlignmentCenter
                numberOfLines = 0
                setFrame(view.bounds)
                autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
            }
            view.addSubview(label)
        }
    },
)

// Presented modally. The content is Compose, so it can close itself.
@NavigationDestination(PresentedViewController::class)
val presentedViewControllerDestination = uiViewControllerDestination<PresentedViewController>(
    metadata = { listOf(UIViewControllerDestination.SupportsPresent()) },
    viewController = {
        EnroUIViewController {
            val navigation = navigationHandle<PresentedViewController>()
            ViewControllerContent(
                title = "Presented modally",
                onClose = { navigation.close() },
            )
        }
    },
)

// Pushed onto the UINavigationController that hosts the app, so it gets the
// system navigation bar and back button.
@NavigationDestination(PushedViewController::class)
val pushedViewControllerDestination = uiViewControllerDestination<PushedViewController>(
    metadata = { listOf(UIViewControllerDestination.SupportsUINavigationView) },
    viewController = {
        EnroUIViewController {
            val navigation = navigationHandle<PushedViewController>()
            ViewControllerContent(
                title = "Pushed onto the UINavigationController",
                onClose = { navigation.close() },
            )
        }
    },
)
// endregion

@Composable
private fun ViewControllerContent(
    title: String,
    onClose: () -> Unit,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Button(onClick = onClose) { Text("Close") }
            }
        }
    }
}
