package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.components.ContainerAcceptPolicy
import dev.enro.core.container.components.ContainerEmptyPolicy
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.destination.compose.container.ComposableContainerRenderer
import dev.enro.destination.compose.container.ComposableContextProvider
import java.io.Closeable

public class ComposableNavigationContainer internal constructor(
    key: NavigationContainerKey,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
    initialBackstack: NavigationBackstack,
    contextProvider: ComposableContextProvider = ComposableContextProvider(
        key = key,
        context = parentContext,
    ),
) : NavigationContainer(
    key = key,
    initialBackstack = initialBackstack,
    context = parentContext,
    interceptor = interceptor,
    animations = animations,
    acceptPolicy = ContainerAcceptPolicy.Default(
        context = parentContext,
        acceptsContextType = ComposableDestination::class,
        acceptsNavigationKey = accept,
    ),
    emptyPolicy = ContainerEmptyPolicy.Default(
        context = parentContext,
        emptyBehavior = emptyBehavior,
    ),
    containerRenderer = ComposableContainerRenderer(
        key = key,
        contextProvider = contextProvider,
        context = parentContext,
    ),
    containerContextProvider = contextProvider
) {
    // We want "Render" to look like it's a Composable function (it's a Composable lambda), so
    // we are uppercasing the first letter of the property name, which triggers a PropertyName lint warning
    @Suppress("PropertyName")
    public val Render: @Composable () -> Unit get() =
        (containerRenderer as ComposableContainerRenderer).Render

    @Composable
    internal fun registerWithContainerManager(
        registrationStrategy: ContainerRegistrationStrategy
    ): Boolean {
        val registration = remember(key, registrationStrategy) {
            val containerManager = context.containerManager
            containerManager.addContainer(this@ComposableNavigationContainer)
            Closeable { destroy() }
        }
        DisposableEffect(key, registrationStrategy) {
            onDispose {
                when (registrationStrategy) {
                    ContainerRegistrationStrategy.DisposeWithComposition -> registration.close()
                    ContainerRegistrationStrategy.DisposeWithLifecycle -> {} // handled by init
                }
            }
        }

        DisposableEffect(key) {
            val containerManager = context.containerManager
            onDispose {
                if (containerManager.activeContainer == this@ComposableNavigationContainer) {
                    val previouslyActiveContainer =
                        backstack.active?.internal?.previouslyActiveContainer?.takeIf { it != key }
                    containerManager.setActiveContainerByKey(previouslyActiveContainer)
                }
            }
        }

        DisposableEffect(key) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_PAUSE) {
                    (containerRenderer as ComposableContainerRenderer).setVisibilityForBackstack(
                        NavigationBackstackTransition(backstack to backstack)
                    )
                }
            }
            context.lifecycle.addObserver(lifecycleObserver)
            onDispose { context.lifecycle.removeObserver(lifecycleObserver) }
        }
        return true
    }

}

@AdvancedEnroApi
public sealed interface ContainerRegistrationStrategy {
    public object DisposeWithComposition : ContainerRegistrationStrategy
    public object DisposeWithLifecycle : ContainerRegistrationStrategy
}