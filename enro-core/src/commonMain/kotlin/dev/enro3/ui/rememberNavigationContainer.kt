package dev.enro3.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro3.EnroController
import dev.enro3.NavigationBackstack
import dev.enro3.NavigationContainer
import dev.enro3.NavigationKey
import dev.enro3.interceptor.NavigationInterceptor
import dev.enro3.interceptor.NoOpNavigationInterceptor

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    backstack: NavigationBackstack,
    // Need to get parent interceptors too from controller
    interceptor: NavigationInterceptor = NoOpNavigationInterceptor,
): NavigationContainer {
    val parent = runCatching { LocalNavigationContainer.current }
    val controller = remember {
        requireNotNull(EnroController.instance) {
            "EnroController instance is not initialized"
        }
    }
    return rememberSaveable(
        saver = NavigationContainerSaver(
            key = key,
            controller = controller,
            parent = parent.getOrNull(),
            interceptor = interceptor,
        ),
    ) {
        NavigationContainer(
            key = key,
            controller = controller,
            backstack = backstack,
            parent = parent.getOrNull(),
            interceptor = interceptor
        )
    }
}

internal class NavigationContainerSaver(
    private val key: NavigationContainer.Key,
    private val controller: EnroController,
    private val parent: NavigationContainer?,
    private val interceptor: NavigationInterceptor,
) : Saver<NavigationContainer, SavedState> {
    override fun restore(value: SavedState): NavigationContainer? {
        val restoredBackstack = value.read {
            getSavedStateList(BackstackKey).map {
                decodeFromSavedState<NavigationKey.Instance<NavigationKey>>(
                    savedState = it,
                    configuration = controller.serializers.savedStateConfiguration,
                )
            }
        }
        return NavigationContainer(
            key = key,
            controller = controller,
            backstack = restoredBackstack,
            parent = parent,
            interceptor = interceptor,
        )
    }

    override fun SaverScope.save(value: NavigationContainer): SavedState? {
        val savedBackstack = value.backstack.value.map { instance ->
            encodeToSavedState(
                value = instance,
                configuration = controller.serializers.savedStateConfiguration
            )
        }
        return savedState {
            putSavedStateList(
                key = BackstackKey,
                value = savedBackstack,
            )
        }
    }

    companion object {
        private const val BackstackKey = "dev.enro3.ui.NavigationContainerSaver.Backstack"
    }
}