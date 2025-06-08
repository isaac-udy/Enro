package dev.enro.core.compose

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationOperation
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext
import dev.enro.core.asPush
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationInstructionFilter
import dev.enro.core.container.acceptAll
import dev.enro.core.container.backstackOf
import dev.enro.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.EmptyBehavior as NewEmptyBehavior
import dev.enro.ui.rememberNavigationContainer as newRememberNavigationContainer


@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    root: dev.enro.core.NavigationKey.SupportsPush,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerState {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = backstackOf(root.asPush()),
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
        filter = filter,
    )
}

@Composable
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    initialBackstack: List<dev.enro.core.NavigationKey.SupportsPush> = emptyList(),
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerState {
    return rememberNavigationContainer(
        key = key,
        initialBackstack = initialBackstack.map {
            it.asPush()
        },
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
        filter = filter,
    )
}

@Composable
@AdvancedEnroApi
@JvmName("rememberNavigationContainerWithBackstack")
public fun rememberNavigationContainer(
    key: NavigationContainer.Key = NavigationContainer.Key("NavigationContainer@${currentCompositeKeyHash}"),
    initialBackstack: NavigationBackstack,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    filter: NavigationInstructionFilter = acceptAll(),
): NavigationContainerState {
    val parentContext = LocalNavigationContext.current
    return newRememberNavigationContainer(
        key = key,
        backstack = initialBackstack,
        emptyBehavior = when (emptyBehavior) {
            is EmptyBehavior.Action -> NewEmptyBehavior(
                isBackHandlerEnabled = { true },
                onPredictiveBackProgress = { true },
                onEmpty = {
                    val keepActive = emptyBehavior.onEmpty()
                    return@NewEmptyBehavior when(keepActive) {
                        true -> NewEmptyBehavior.Result.Cancel()
                        else -> NewEmptyBehavior.Result.Continue()
                    }
                }
            )

            EmptyBehavior.AllowEmpty -> NewEmptyBehavior.allowEmpty()
            EmptyBehavior.CloseParent -> NewEmptyBehavior(
                isBackHandlerEnabled = { true },
                onPredictiveBackProgress = { true },
                onEmpty = {
                    NewEmptyBehavior.Result.CancelAnd {
                        when (parentContext) {
                            is ContainerContext -> {
                                val parentContainer = parentContext.container
                                parentContainer.backstack.lastOrNull()?.let {
                                    parentContainer.execute(parentContext, NavigationOperation.Close(it))
                                }
                            }
                            is DestinationContext<*> -> {
                                val parentContainer = parentContext.parent.container
                                parentContainer.backstack.lastOrNull()?.let {
                                    parentContainer.execute(parentContext, NavigationOperation.Close(it))
                                }
                            }
                            is RootContext -> {
                                (parentContext.parent as? Activity)?.finish()
                            }
                        }
                    }
                }
            )

            EmptyBehavior.ForceCloseParent -> NewEmptyBehavior(
                isBackHandlerEnabled = { true },
                onPredictiveBackProgress = { true },
                onEmpty = {
                    NewEmptyBehavior.Result.CancelAnd {
                        when (parentContext) {
                            is ContainerContext -> {
                                val parentContainer = parentContext.container
                                parentContainer.backstack.lastOrNull()?.let {
                                    parentContainer.execute(parentContext, NavigationOperation.Close(it))
                                }
                            }
                            is DestinationContext<*> -> {
                                val parentContainer = parentContext.parent.container
                                parentContainer.backstack.lastOrNull()?.let {
                                    parentContainer.execute(parentContext, NavigationOperation.Close(it))
                                }
                            }
                            is RootContext -> {
                                (parentContext.parent as? Activity)?.finish()
                            }
                        }
                    }
                }
            )
        },
        interceptor = navigationInterceptor(interceptor),
        filter = filter,
    )
}