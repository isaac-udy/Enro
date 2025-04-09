package dev.enro.destination.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.NavigationContext
import dev.enro.core.navigationContext

public val navigationContext: NavigationContext<*>
    @Composable
    get() {
        if (LocalInspectionMode.current) error("Not able to access navigationContext when LocalInspectionMode.current is 'true'")

        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
            "Failed to get navigationContext in Composable: LocalViewModelStoreOwner was null"
        }
        return remember(viewModelStoreOwner) {
            requireNotNull(viewModelStoreOwner.navigationContext) {
                "Failed to get navigationContext in Composable: ViewModelStore owner does not have a NavigationContext reference"
            }
        }
    }
