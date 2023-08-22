package dev.enro.destination.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.NavigationContext
import dev.enro.core.addOpenInstruction
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.navigationController
import dev.enro.core.navigationContext
import dev.enro.core.parentContainer
import dev.enro.destination.compose.destination.activity

internal fun <ContextType : ComposableDestination> ComposeContext(
    contextReference: ContextType,
): NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.owner.activity.application.navigationController },
        getParentContext = { contextReference.owner.parentContainer.context },
        getArguments = { bundleOf().addOpenInstruction(contextReference.owner.instruction) },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
    )
}

@PublishedApi
@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : ComposableDestination> T.navigationContext: NavigationContext<T>
    get() = context as NavigationContext<T>

public val navigationContext: NavigationContext<*>
    @Composable
    get() {
        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
            "Failed to get navigationContext in Composable: LocalViewModelStoreOwner was null"
        }
        return remember(viewModelStoreOwner) {
            requireNotNull(viewModelStoreOwner.navigationContext) {
                "Failed to get navigationContext in Composable: ViewModelStore owner does not have a NavigationContext reference"
            }
        }
    }

public val ComposableDestination.containerManager: NavigationContainerManager
    get() = context.containerManager

public val containerManager: NavigationContainerManager
    @Composable
    get() {
        val viewModelStoreOwner = LocalViewModelStoreOwner.current!!

        val context = LocalContext.current
        val view = LocalView.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // The navigation context attached to a NavigationHandle may change when the Context, View,
        // or LifecycleOwner changes, so we're going to re-query the navigation context whenever
        // any of these change, to ensure the container always has an up-to-date NavigationContext
        return remember(context, view, lifecycleOwner) {
            viewModelStoreOwner
                .navigationContext!!
                .containerManager
        }
    }

public val ComposableDestination.parentContainer: NavigationContainer?
    get() = context.parentContainer()

public val parentContainer: NavigationContainer?
    @Composable
    get() {
        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
            "Failed to get parentContainer in Composable: LocalViewModelStoreOwner was null"
        }
        return remember {
            viewModelStoreOwner
                .navigationContext
                ?.parentContainer()
        }
    }