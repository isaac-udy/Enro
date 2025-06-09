package dev.enro.ui

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
import dev.enro.EnroController
import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationContainerFilter
import dev.enro.NavigationKey
import dev.enro.acceptAll
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.interceptor.NoOpNavigationInterceptor
import dev.enro.ui.decorators.NavigationSavedStateHolder
import kotlinx.serialization.PolymorphicSerializer

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    backstack: NavigationBackstack,
    emptyBehavior: EmptyBehavior = EmptyBehavior.preventEmpty(),
    interceptor: NavigationInterceptor = NoOpNavigationInterceptor,
    filter: NavigationContainerFilter = acceptAll(),
): NavigationContainerState {
    val parentContext = LocalNavigationContext.current
    require(parentContext is RootContext || parentContext is DestinationContext<*>) {
        "NavigationContainer can only be used within a RootContext or DestinationContext"
    }
    val controller = remember {
        requireNotNull(EnroController.instance) {
            "EnroController instance is not initialized"
        }
    }
    val container = rememberSaveable(
        saver = NavigationContainerSaver(
            key = key,
            controller = controller,
        ),
    ) {
        NavigationContainer(
            key = key,
            controller = controller,
            backstack = backstack,
        )
    }
    DisposableEffect(container, filter) {
        container.setFilter(filter)
        onDispose {
            container.clearFilter(filter)
        }
    }

    DisposableEffect(container, emptyBehavior) {
        container.addEmptyInterceptor(emptyBehavior.interceptor)
        onDispose {
            container.removeEmptyInterceptor(emptyBehavior.interceptor)
        }
    }

    DisposableEffect(container, interceptor) {
        container.addInterceptor(interceptor)
        onDispose {
            container.removeInterceptor(interceptor)
        }
    }

    val context = remember(container, parentContext) {
        ContainerContext(
            container = container,
            parent = parentContext,
        )
    }

    // Register/unregister with parent context
    DisposableEffect(container, parentContext) {
        parentContext.registerChild(context)
        onDispose {
            parentContext.unregisterChild(context)
        }
    }
    val savedState = rememberSaveable(
        saver = NavigationSavedStateHolder.Saver
    ) {
        NavigationSavedStateHolder(savedState())
    }
    val containerState = remember(container) {
        NavigationContainerState(
            container = container,
            emptyBehavior = emptyBehavior,
            context = context,
            savedStateHolder = savedState,
        )
    }
    val destinations = rememberDecoratedDestinations(
        controller = controller,
        backstack = containerState.backstack,
        savedStateHolder = savedState,
        isSettled = containerState.isSettled,
    )
    containerState.destinations = destinations
    return containerState
}

internal class NavigationContainerSaver(
    private val key: NavigationContainer.Key,
    private val controller: EnroController,
) : Saver<NavigationContainer, SavedState> {
    override fun restore(value: SavedState): NavigationContainer? {
        val restoredBackstack = value.read {
            getSavedStateList(BackstackKey).map {
                decodeFromSavedState(
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
        )
    }

    override fun SaverScope.save(value: NavigationContainer): SavedState? {
        val savedBackstack = value.backstack.map { instance ->
            encodeToSavedState(
                serializer = NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class)),
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
        private const val BackstackKey = "dev.enro.ui.NavigationContainerSaver.Backstack"
    }
}
