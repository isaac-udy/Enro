package dev.enro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.context.RootContext
import dev.enro.handle.RootNavigationHandle
import dev.enro.handle.getOrCreateNavigationHandleHolder
import dev.enro.viewmodel.EnroWrappedViewModelStoreOwner
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * A composable that provides the root navigation context for a browser-based Enro application.
 *
 * This is the main entry point for using Enro in a wasmJs/browser environment. It sets up the
 * required navigation context, view model store, and navigation handle that are needed for
 * Enro navigation to work.
 *
 * Usage:
 * ```kotlin
 * fun main() {
 *     MyNavigationComponent.installNavigationController(document)
 *     ComposeViewPort {
 *         EnroBrowserContent {
 *             // Your app content here
 *             MyApplicationContent()
 *         }
 *     }
 * }
 * ```
 *
 * @param suppressEscapeAsBack When `true` (the default), pressing the
 *   Escape key inside the Compose composition is consumed before
 *   Compose Multiplatform's built-in `BackNavigationEventInput` can
 *   translate it into a back-navigation event. Browser back/forward via
 *   the history API is unaffected (it routes through `popstate`, not the
 *   navigation event dispatcher). Set to `false` if your app deliberately
 *   wants Escape→back behaviour.
 * @param content The composable content of your application
 */
@Composable
public fun EnroBrowserContent(
    suppressEscapeAsBack: Boolean = true,
    content: @Composable EnroBrowserScope.() -> Unit,
) {
    val instance = remember { GenericBrowserKey.asInstance() }
    val enroController = remember {
        requireNotNull(EnroController.instance) {
            "EnroController instance is not available. Ensure that Enro is properly initialized before calling EnroBrowserContent."
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val localViewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
        "LocalViewModelStoreOwner is not provided. Ensure that the composable is hosted within a ViewModelStoreOwner."
    }
    val viewModelStoreOwner = remember(localViewModelStoreOwner) {
        EnroWrappedViewModelStoreOwner(
            controller = enroController,
            viewModelStoreOwner = localViewModelStoreOwner,
            savedStateRegistryOwner = null
        )
    }
    val activeChildId = remember { mutableStateOf<String?>(null) }
    val browserId = remember { Uuid.random().toString() }
    val (context, navigationHandle) = remember(viewModelStoreOwner) {
        val context = RootContext(
            id = "Browser(${instance.key::class.simpleName})@$browserId",
            parent = Unit, // Browser tabs don't have a parent object like UIViewController
            controller = enroController,
            lifecycleOwner = lifecycleOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            defaultViewModelProviderFactory = viewModelStoreOwner,
            activeChildId = activeChildId,
        )

        val holder = viewModelStoreOwner.getOrCreateNavigationHandleHolder {
            RootNavigationHandle(
                instance = instance,
                savedStateHandle = createSavedStateHandle(),
            )
        }
        val navigationHandle = holder.navigationHandle
        require(navigationHandle is RootNavigationHandle)
        navigationHandle.bindContext(context)

        return@remember context to navigationHandle
    }

    DisposableEffect(context) {
        enroController.rootContextRegistry.register(context)
        onDispose {
            enroController.rootContextRegistry.unregister(context)
        }
    }

    val browserScope = remember(navigationHandle) {
        EnroBrowserScope(navigationHandle)
    }

    CompositionLocalProvider(
        LocalRootContext provides context,
        LocalNavigationContext provides context,
        LocalNavigationHandle provides navigationHandle,
        LocalViewModelStoreOwner provides viewModelStoreOwner,
    ) {
        if (suppressEscapeAsBack) {
            // Consumes Escape KeyDown via Compose's preview key chain
            // (outer → inner) before Compose Multiplatform's
            // BackNavigationEventInput can translate it into a back event.
            // Inner Popups/Dialogs run in their own focus tree and still
            // see Escape — only the main composition's Escape is swallowed.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onPreviewKeyEvent { event ->
                        event.type == KeyEventType.KeyDown && event.key == Key.Escape
                    },
            ) {
                browserScope.content()
            }
        } else {
            browserScope.content()
        }
    }
}

/**
 * Scope for the EnroBrowserContent composable, providing access to the root navigation handle.
 */
public class EnroBrowserScope internal constructor(
    public val navigation: NavigationHandle<*>,
) {
    public val instance: NavigationKey.Instance<*>
        get() = navigation.instance

    public val key: NavigationKey
        get() = navigation.key
}

@Serializable
internal object GenericBrowserKey : NavigationKey
