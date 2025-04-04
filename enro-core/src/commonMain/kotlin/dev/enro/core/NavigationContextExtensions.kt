package dev.enro.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.container.NavigationContainer
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ActiveNavigationHandleReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


internal val NavigationContext<*>.allParentContexts: List<NavigationContext<*>>
    get() {
        val parents = mutableListOf<NavigationContext<*>>()
        var parent: NavigationContext<*>? = parentContext
        while (parent != null) {
            parents.add(parent)
            parent = parent.parentContext
        }
        return parents
    }

internal val NavigationContext<*>.isActive: Flow<Boolean>
    get() {
        val id = instruction.instructionId
        return controller.dependencyScope.get<ActiveNavigationHandleReference>()
            .activeNavigationIdFlow
            .map { it == id }
            .distinctUntilChanged()
    }

public val ComposableDestination.parentContainer: NavigationContainer? get() = navigationContext.parentContainer()

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
