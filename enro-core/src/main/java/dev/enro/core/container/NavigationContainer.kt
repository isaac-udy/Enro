package dev.enro.core.container

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import dev.enro.animation.NavigationAnimation
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.container.components.ContainerAcceptPolicy
import dev.enro.core.container.components.ContainerActivePolicy
import dev.enro.core.container.components.ContainerAnimationPolicy
import dev.enro.core.container.components.ContainerContextProvider
import dev.enro.core.container.components.ContainerEmptyPolicy
import dev.enro.core.container.components.ContainerRenderer
import dev.enro.core.container.components.ContainerState
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import kotlinx.coroutines.flow.StateFlow

public abstract class NavigationContainer(
    public val key: NavigationContainerKey,
    initialBackstack: NavigationBackstack,
    public val context: NavigationContext<*>,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
    private val emptyPolicy: ContainerEmptyPolicy,
    private val acceptPolicy: ContainerAcceptPolicy,
    private val activePolicy: ContainerActivePolicy,
    private val animationPolicy: ContainerAnimationPolicy,
    private val containerRenderer: ContainerRenderer,
    private val containerContextProvider: ContainerContextProvider<*>,
) : NavigationContainerContext {

    internal val dependencyScope by lazy {
        NavigationContainerScope(
            owner = this,
            animations = animations
        )
    }

    internal val state = ContainerState(
        key = key,
        context = context,
        initialBackstack = initialBackstack,
        savables = emptyList(),
        acceptPolicy = acceptPolicy,
        emptyPolicy = emptyPolicy,
        activePolicy = activePolicy,
    ).also {
        containerRenderer.bind(it)
    }

    internal val interceptor = NavigationInterceptorBuilder()
        .apply(interceptor)
        .build()

    public val childContext: NavigationContext<*>?
        get() = containerContextProvider.getActiveNavigationContext(state.backstack)

    public open val isVisible: Boolean
        get() = containerRenderer.isVisible

    public override val isActive: Boolean
        get() = activePolicy.isActive

    public override fun setActive() {
        activePolicy.setActive()
    }

    public override val backstackFlow: StateFlow<NavigationBackstack> get() = state.backstackFlow
    public override val backstack: NavigationBackstack get() = state.backstack

    @CallSuper
    public final override fun save(): Bundle {
        return state.save()
    }

    @CallSuper
    public final override fun restore(bundle: Bundle) {
        state.restore(bundle)
    }

    @MainThread
    public final override fun setBackstack(backstack: NavigationBackstack) {
        state.setBackstack(backstack)
    }

    public fun accept(
        navigationKey: NavigationKey
    ) : Boolean {
        return acceptPolicy.accepts(navigationKey)
    }

    public fun accept(
        instruction: AnyOpenInstruction
    ): Boolean {
        return acceptPolicy.accepts(instruction)
    }

    public fun getAnimationsForEntering(instruction: AnyOpenInstruction): NavigationAnimation {
        return animationPolicy.getAnimationsForEntering(this, instruction)
    }

    public fun getAnimationsForExiting(instruction: AnyOpenInstruction): NavigationAnimation {
        return animationPolicy.getAnimationsForEntering(this, instruction)
    }
}