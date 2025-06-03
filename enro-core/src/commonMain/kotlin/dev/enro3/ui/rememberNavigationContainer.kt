package dev.enro3.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import kotlinx.serialization.PolymorphicSerializer

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    backstack: NavigationBackstack,
    // Need to get parent interceptors too from controller
    interceptor: NavigationInterceptor = NoOpNavigationInterceptor,
): NavigationContainerState {
    val parent = runCatching { LocalNavigationContainer.current }
    val parentContext = runCatching { LocalNavigationContext.current }.getOrNull()
    val controller = remember {
        requireNotNull(EnroController.instance) {
            "EnroController instance is not initialized"
        }
    }
    val container = rememberSaveable(
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

    // Register/unregister with parent context
    DisposableEffect(container, parentContext) {
        parentContext?.registerChildContainer(container)
        onDispose {
            parentContext?.unregisterChildContainer(container)
        }
    }

    val containerState = remember(container) {
        NavigationContainerState(container = container)
    }
    val destinations = rememberDecoratedDestinations(
        controller = controller,
        backstack = containerState.backstack,
        isSettled = containerState.isSettled,
    )
    containerState.destinations = destinations
    return containerState
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
                    deserializer = NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class)),
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
                serializer = NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class)),
                value = instance as NavigationKey.Instance<NavigationKey>,
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
