package dev.enro.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import dev.enro.ui.EnroBrowserRoot
import dev.enro.ui.EnroBrowserScope
import kotlinx.browser.document
import org.w3c.dom.Document
import org.w3c.dom.events.Event

/**
 * The root of an Enro app rendered with Compose HTML. Call it inside `renderComposable`, after
 * installing the navigation controller:
 *
 * ```kotlin
 * fun main() {
 *     MyNavigationComponent.installNavigationController(document)
 *     renderComposable(rootElementId = "root") {
 *         EnroHtmlContent {
 *             val container = rememberNavigationContainer(backstack = backstackOf(Home.asInstance()))
 *             HtmlNavigationDisplay(container)
 *         }
 *     }
 * }
 * ```
 *
 * `renderComposable` provides no lifecycle, ViewModelStore or SavedStateRegistry owner, so
 * [EnroHtmlContent] provides one for the page. The page lifecycle is RESUMED while the document
 * is visible and CREATED while it is hidden.
 */
@Composable
public fun EnroHtmlContent(
    content: @Composable EnroBrowserScope.() -> Unit,
) {
    val page = remember { HtmlPageOwner() }
    DisposableEffect(page) {
        val onVisibilityChange: (Event) -> Unit = { page.updateLifecycleFromVisibility() }
        document.addEventListener("visibilitychange", onVisibilityChange)
        onDispose {
            document.removeEventListener("visibilitychange", onVisibilityChange)
            page.destroy()
        }
    }
    CompositionLocalProvider(
        LocalLifecycleOwner provides page,
        LocalViewModelStoreOwner provides page,
        LocalSavedStateRegistryOwner provides page,
    ) {
        EnroBrowserRoot(content)
    }
}

private class HtmlPageOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)
    override val viewModelStore: ViewModelStore = ViewModelStore()

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    init {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        updateLifecycleFromVisibility()
    }

    fun updateLifecycleFromVisibility() {
        lifecycle.currentState = if (document.isHidden) Lifecycle.State.CREATED else Lifecycle.State.RESUMED
    }

    fun destroy() {
        lifecycle.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }
}

// The Kotlin/JS DOM declarations have no Document.hidden.
private val Document.isHidden: Boolean
    get() = asDynamic().hidden == true
