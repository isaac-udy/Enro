package dev.enro.core

import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import dev.enro.core.container.originalNavigationDirection


internal data class OpeningTransition(
    val priority: Int,
    val transition: (exiting: AnyOpenInstruction?, entering: AnyOpenInstruction) -> NavigationAnimationTransition?
)

internal data class ClosingTransition(
    val priority: Int,
    val transition: (exiting: AnyOpenInstruction, entering: AnyOpenInstruction?) -> NavigationAnimationTransition?
)

internal data class NavigationAnimationOverride(
    val parent: NavigationAnimationOverride?,
    val opening: List<OpeningTransition>,
    val closing: List<ClosingTransition>,
)

public class NavigationAnimationOverrideBuilder {
    private val opening = mutableListOf<OpeningTransition>()
    private val closing = mutableListOf<ClosingTransition>()

    public fun addOpeningTransition(priority: Int, transition: (exiting: AnyOpenInstruction?, entering: AnyOpenInstruction) -> NavigationAnimationTransition?) {
        opening.add(OpeningTransition(priority, transition))
    }

    public fun addClosingTransition(priority: Int, transition: (exiting: AnyOpenInstruction, entering: AnyOpenInstruction?) -> NavigationAnimationTransition?) {
        closing.add(ClosingTransition(priority, transition))
    }

    public fun direction(
        direction: NavigationDirection,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }
    }

    public fun direction(
        direction: NavigationDirection,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter,
        returnExiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }

        addClosingTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction == null) return@addClosingTransition null
            if (enteringInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            NavigationAnimationTransition(
                entering = returnEntering,
                exiting = returnExiting,
            )
        }
    }

    public inline fun <reified Key : NavigationKey> transitionTo(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(PARTIAL_KEY_PRIORITY) { _, enteringInstruction ->
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Key) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }
    }

    public inline fun <reified Key : NavigationKey> transitionTo(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter,
        returnExiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(PARTIAL_KEY_PRIORITY) { _, enteringInstruction ->
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Key) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }

        addClosingTransition(PARTIAL_KEY_PRIORITY) { exitingInstruction, _ ->
            if (direction != null && exitingInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            if (exitingInstruction.navigationKey !is Key) return@addClosingTransition null
            NavigationAnimationTransition(
                entering = returnEntering,
                exiting = returnExiting,
            )
        }
    }

    public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> transitionBetween(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(EXACT_KEY_PRIORITY) { exitingInstruction, enteringInstruction ->
            if (exitingInstruction == null) return@addOpeningTransition null
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (exitingInstruction.navigationKey !is Exit) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Enter) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }
    }

    public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> transitionBetween(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter,
        returnExiting: NavigationAnimation.Exit,
    ) {
        addOpeningTransition(EXACT_KEY_PRIORITY) { exitingInstruction, enteringInstruction ->
            if (exitingInstruction == null) return@addOpeningTransition null
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (exitingInstruction.navigationKey !is Exit) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Enter) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }

        addClosingTransition(EXACT_KEY_PRIORITY) { exitingInstruction, enteringInstruction ->
            if (enteringInstruction == null) return@addClosingTransition null
            if (direction != null && exitingInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            if (exitingInstruction.navigationKey !is Enter) return@addClosingTransition null
            if (enteringInstruction.navigationKey !is Exit) return@addClosingTransition null
            NavigationAnimationTransition(
                entering = returnEntering,
                exiting = returnExiting,
            )
        }
    }

    internal fun build(parent: NavigationAnimationOverride?): NavigationAnimationOverride {
        return NavigationAnimationOverride(
            parent = parent,
            opening = opening,
            closing = closing
        )
    }

    public companion object {
        @PublishedApi
        internal const val DEFAULT_PRIORITY: Int = 0
        @PublishedApi
        internal const val DIRECTION_PRIORITY: Int = 10
        @PublishedApi
        internal const val PARTIAL_KEY_PRIORITY: Int = 20
        @PublishedApi
        internal const val EXACT_KEY_PRIORITY: Int = 30
    }
}

// Resource extensions
public fun NavigationAnimationOverrideBuilder.direction(
    direction: NavigationDirection,
    @AnimRes @AnimatorRes entering: Int,
    @AnimRes @AnimatorRes exiting: Int,
) {
    direction(
        direction = direction,
        entering = NavigationAnimation.Resource(entering),
        exiting = NavigationAnimation.Resource(exiting),
    )
}

public fun NavigationAnimationOverrideBuilder.direction(
    direction: NavigationDirection,
    @AnimRes @AnimatorRes entering: Int,
    @AnimRes @AnimatorRes exiting: Int,
    @AnimRes @AnimatorRes returnEntering: Int,
    @AnimRes @AnimatorRes returnExiting: Int,
) {
    direction(
        direction = direction,
        entering = NavigationAnimation.Resource(entering),
        exiting = NavigationAnimation.Resource(exiting),
        returnEntering = NavigationAnimation.Resource(returnEntering),
        returnExiting = NavigationAnimation.Resource(returnExiting),
    )
}

public inline fun <reified Key : NavigationKey> NavigationAnimationOverrideBuilder.transitionTo(
    direction: NavigationDirection? = null,
    @AnimRes @AnimatorRes entering: Int,
    @AnimRes @AnimatorRes exiting: Int,
) {
    transitionTo<Key>(
        direction = direction,
        entering = NavigationAnimation.Resource(entering),
        exiting = NavigationAnimation.Resource(exiting),
    )
}

public inline fun <reified Key : NavigationKey> NavigationAnimationOverrideBuilder.transitionTo(
    direction: NavigationDirection? = null,
    @AnimRes @AnimatorRes entering: Int,
    @AnimRes @AnimatorRes exiting: Int,
    @AnimRes @AnimatorRes returnEntering: Int,
    @AnimRes @AnimatorRes returnExiting: Int,
) {
    transitionTo<Key>(
        direction = direction,
        entering = NavigationAnimation.Resource(entering),
        exiting = NavigationAnimation.Resource(exiting),
        returnEntering = NavigationAnimation.Resource(returnEntering),
        returnExiting = NavigationAnimation.Resource(returnExiting),
    )
}

public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> NavigationAnimationOverrideBuilder.transitionBetween(
    direction: NavigationDirection? = null,
    @AnimRes @AnimatorRes entering: Int,
    @AnimRes @AnimatorRes exiting: Int,
) {
    transitionBetween<Exit, Enter>(
        direction = direction,
        entering = NavigationAnimation.Resource(entering),
        exiting = NavigationAnimation.Resource(exiting),
    )
}

public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> NavigationAnimationOverrideBuilder.transitionBetween(
    direction: NavigationDirection? = null,
    @AnimRes @AnimatorRes entering: Int,
    @AnimRes @AnimatorRes exiting: Int,
    @AnimRes @AnimatorRes returnEntering: Int,
    @AnimRes @AnimatorRes returnExiting: Int,
) {
    transitionBetween<Exit, Enter>(
        direction = direction,
        entering = NavigationAnimation.Resource(entering),
        exiting = NavigationAnimation.Resource(exiting),
        returnEntering = NavigationAnimation.Resource(returnEntering),
        returnExiting = NavigationAnimation.Resource(returnExiting),
    )
}

// Composable transition extensions
public fun NavigationAnimationOverrideBuilder.direction(
    direction: NavigationDirection,
    entering: EnterTransition,
    exiting: ExitTransition,
) {
    direction(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
    )
}

public fun NavigationAnimationOverrideBuilder.direction(
    direction: NavigationDirection,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition,
    returnExiting: ExitTransition,
) {
    direction(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
        returnEntering = NavigationAnimation.Composable(returnEntering),
        returnExiting = NavigationAnimation.Composable(returnExiting),
    )
}

public inline fun <reified Key : NavigationKey> NavigationAnimationOverrideBuilder.transitionTo(
    direction: NavigationDirection? = null,
    entering: EnterTransition,
    exiting: ExitTransition,
) {
    transitionTo<Key>(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
    )
}

public inline fun <reified Key : NavigationKey> NavigationAnimationOverrideBuilder.transitionTo(
    direction: NavigationDirection? = null,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition,
    returnExiting: ExitTransition,
) {
    transitionTo<Key>(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
        returnEntering = NavigationAnimation.Composable(returnEntering),
        returnExiting = NavigationAnimation.Composable(returnExiting),
    )
}

public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> NavigationAnimationOverrideBuilder.transitionBetween(
    direction: NavigationDirection? = null,
    entering: EnterTransition,
    exiting: ExitTransition,
) {
    transitionBetween<Exit, Enter>(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
    )
}

public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> NavigationAnimationOverrideBuilder.transitionBetween(
    direction: NavigationDirection? = null,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition,
    returnExiting: ExitTransition,
) {
    transitionBetween<Exit, Enter>(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
        returnEntering = NavigationAnimation.Composable(returnEntering),
        returnExiting = NavigationAnimation.Composable(returnExiting),
    )
}