package dev.enro.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass

/**
 * @deprecated NavigationAnimationOverrides are no longer supported to be set on containers
 * or at a global level. Instead, you should set a [dev.enro.ui.NavigationAnimations] object
 * on a NavigationDisplay object directly.
 */
@Deprecated(
    message = "NavigationAnimationOverrides are no longer supported to be set on containers or at a global level. Instead, you should set a dev.enro.ui.NavigationAnimations object on a NavigationDisplay object directly.",
    level = DeprecationLevel.WARNING
)
public class NavigationAnimationOverrideBuilder {
    
    @Deprecated(
        message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
        level = DeprecationLevel.ERROR
    )
    public fun addOpeningTransition(
        priority: Int,
        transition: (exiting: Any?, entering: Any) -> Any?
    ) {
    }
    
    @Deprecated(
        message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
        level = DeprecationLevel.ERROR
    )
    public fun addClosingTransition(
        priority: Int,
        transition: (exiting: Any, entering: Any?) -> Any?
    ) {
    }
    
    @Deprecated(
        message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
        level = DeprecationLevel.ERROR
    )
    public fun <T : Any> defaults(
        type: KClass<T>,
        defaults: Any,
    ) {
    }
    
    @Deprecated(
        message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
        level = DeprecationLevel.ERROR
    )
    public inline fun <reified T : Any> defaults(
        defaults: Any
    ) {
    }
    
    @Deprecated(
        message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
        level = DeprecationLevel.ERROR
    )
    public fun direction(
        direction: NavigationDirection,
        animation: Any,
        returnAnimation: Any? = null,
    ) {
    }
    
    @Deprecated(
        message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
        level = DeprecationLevel.ERROR
    )
    public inline fun <reified Key : NavigationKey> transitionTo(
        direction: NavigationDirection? = null,
        animation: Any,
        returnAnimation: Any? = null,
    ) {
    }
    
    @Deprecated(
        message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
        level = DeprecationLevel.ERROR
    )
    public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> transitionBetween(
        direction: NavigationDirection? = null,
        animation: Any,
        returnAnimation: Any? = null,
    ) {
    }
}

@Deprecated(
    message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
    level = DeprecationLevel.ERROR
)
public fun NavigationAnimationOverrideBuilder.direction(
    direction: NavigationDirection,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition? = entering,
    returnExiting: ExitTransition? = exiting,
) {
}

@Deprecated(
    message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
    level = DeprecationLevel.ERROR
)
public inline fun <reified Key : NavigationKey> NavigationAnimationOverrideBuilder.transitionTo(
    direction: NavigationDirection? = null,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition? = entering,
    returnExiting: ExitTransition? = exiting,
) {
}

@Deprecated(
    message = "Please read the deprecation message on NavigationAnimationOverrideBuilder class itself",
    level = DeprecationLevel.ERROR
)
public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> NavigationAnimationOverrideBuilder.transitionBetween(
    direction: NavigationDirection? = null,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition? = entering,
    returnExiting: ExitTransition? = exiting,
) {
}