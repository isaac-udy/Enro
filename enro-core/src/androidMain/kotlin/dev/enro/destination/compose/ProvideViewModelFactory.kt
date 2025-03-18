package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.viewmodel.withNavigationHandle

@Composable
public fun ProvideViewModelFactory(
    factory: ViewModelProvider.Factory,
    content: @Composable () -> Unit,
) {
    val enroFactory = factory.withNavigationHandle()
    val localViewModelStoreOwner = LocalViewModelStoreOwner.current
    val wrappedViewModelStoreOwner = remember(enroFactory, localViewModelStoreOwner) {
        WrappedViewModelStoreOwner(
            wrapped = requireNotNull(localViewModelStoreOwner) {
                "Failed to ProvideViewModelFactory: LocalViewModelStoreOwner was not found"
            },
            factory = enroFactory,
        )
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides wrappedViewModelStoreOwner
    ) {
        content()
    }
}

internal class WrappedViewModelStoreOwner(
    val wrapped: ViewModelStoreOwner,
    val factory: ViewModelProvider.Factory
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    override val viewModelStore: ViewModelStore
        get() = wrapped.viewModelStore

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = factory

    override val defaultViewModelCreationExtras: CreationExtras
        get() = when (wrapped) {
            is HasDefaultViewModelProviderFactory -> wrapped.defaultViewModelCreationExtras
            else -> super.defaultViewModelCreationExtras
        }
}
