package dev.enro.core.fragment.container

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import dev.enro.animation.DefaultAnimations
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.animation.NavigationAnimationTransition
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.components.ContainerAcceptPolicy
import dev.enro.core.container.components.ContainerActivePolicy
import dev.enro.core.container.components.ContainerAnimationPolicy
import dev.enro.core.container.components.ContainerEmptyPolicy
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.destination.fragment.container.FragmentContainerRenderer
import dev.enro.destination.fragment.container.FragmentContextProvider
import dev.enro.extensions.animate

public class FragmentNavigationContainer internal constructor(
    @IdRes public val containerId: Int,
    key: NavigationContainerKey = NavigationContainerKey.FromId(containerId),
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
    initialBackstack: NavigationBackstack,
    containerContextProvider: FragmentContextProvider = FragmentContextProvider(
        containerId = containerId,
        context = parentContext,
    )
) : NavigationContainer(
    key = key,
    initialBackstack = initialBackstack,
    context = parentContext,
    interceptor = interceptor,
    animations = animations,
    acceptPolicy = ContainerAcceptPolicy.Default(
        context = parentContext,
        acceptsContextType = Fragment::class,
        acceptsNavigationKey = accept,
    ),
    activePolicy = ContainerActivePolicy.Default(
        key = key,
        context = parentContext
    ),
    emptyPolicy = ContainerEmptyPolicy.Default(
        context = parentContext,
        emptyBehavior = emptyBehavior,
    ),
    animationPolicy = ContainerAnimationPolicy.Default(),
    containerRenderer = FragmentContainerRenderer(
        containerId = containerId,
        context = parentContext,
        contextProvider = containerContextProvider,
    ),
    containerContextProvider = containerContextProvider
) {
    override var isVisible: Boolean
        get() = super.isVisible
        set(value) {
            containerView?.isVisible = value
        }
}

//    public override fun save(): Bundle {
//        val savedState = super.save()
//        backstack.asFragmentAndInstruction()
//            .forEach {
//                val fragmentState = fragmentManager.saveFragmentInstanceState(it.fragment)
//                savedState.putParcelable(
//                    "${FRAGMENT_STATE_PREFIX_KEY}${it.instruction.instructionId}",
//                    fragmentState
//                )
//            }
//        savedState.putStringArrayList(OWNED_FRAGMENTS_KEY, ArrayList(ownedFragments))
//        return savedState
//    }
//
//    public override fun restore(bundle: Bundle) {
//        bundle.keySet().forEach { key ->
//            if (!key.startsWith(FRAGMENT_STATE_PREFIX_KEY)) return@forEach
//            val fragmentState =
//                bundle.getParcelableCompat<Fragment.SavedState>(key) ?: return@forEach
//            val instructionId = key.removePrefix(FRAGMENT_STATE_PREFIX_KEY)
//            restoredFragmentStates[instructionId] = fragmentState
//        }
//        ownedFragments.addAll(bundle.getStringArrayList(OWNED_FRAGMENTS_KEY).orEmpty())
//        super.restore(bundle)
//    }
//private companion object {
//    private const val FRAGMENT_STATE_PREFIX_KEY = "FragmentState@"
//    private const val OWNED_FRAGMENTS_KEY = "OWNED_FRAGMENTS_KEY"
//}

public val FragmentNavigationContainer.containerView: View?
    get() {
        return when (context.contextReference) {
            is Activity -> context.contextReference.findViewById(containerId)
            is Fragment -> context.contextReference.view?.findViewById(containerId)
            else -> null
        }
    }

public fun FragmentNavigationContainer.setVisibilityAnimated(
    isVisible: Boolean,
    animations: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = DefaultAnimations.ForView.presentEnter,
        exiting = DefaultAnimations.ForView.presentCloseExit,
    )
) {
    val view = containerView ?: return
    if (!view.isVisible && !isVisible) return
    if (view.isVisible && isVisible) return

    view.animate(
        animOrAnimator = when (isVisible) {
            true -> animations.entering.asResource(view.context.theme).id
            false -> animations.exiting.asResource(view.context.theme).id
        },
        onAnimationStart = {
            view.translationZ = if (isVisible) 0f else -1f
            view.isVisible = true
        },
        onAnimationEnd = {
            view.isVisible = isVisible
        }
    )
}
