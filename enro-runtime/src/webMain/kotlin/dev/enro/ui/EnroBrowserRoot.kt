package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.InternalEnroApi
import dev.enro.asInstance
import dev.enro.context.RootContext
import dev.enro.handle.RootNavigationHandle
import dev.enro.handle.getOrCreateNavigationHandleHolder
import dev.enro.viewmodel.EnroWrappedViewModelStoreOwner
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Provides the browser's root navigation context, navigation handle and ViewModelStoreOwner to
 * [content]. The renderer-specific browser entry points, such as `EnroBrowserContent`, build on it.
 */
@InternalEnroApi
@Composable
public fun EnroBrowserRoot(
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
        browserScope.content()
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
