package dev.enro.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.originalNavigationDirection


public class NavigationAnimationOverrideBuilder {
    private val opening = mutableListOf<OpeningTransition>()
    private val closing = mutableListOf<ClosingTransition>()

    /**
     * This is an Advanced Enro API. In most situations, [direction], [transitionTo] and
     * [transitionBetween] should provide the functionality required for standard navigation animations.
     *
     * [addOpeningTransition]'s transition is defined by a lambda which returns a nullable
     * [NavigationAnimationTransition]. A [NavigationAnimationTransition] defines the [NavigationAnimation]
     * to use for both the entering and exiting instruction.
     *
     * When a NavigationInstruction is opened, all the transition lambdas registered in the relevant
     * contexts will be iterated through, passing in the instruction that is exiting (if there is one)
     * and the instruction that is entering (which will always exist when an instruction is opened).
     * The first of these lambdas to return a non-null [NavigationAnimationTransition]
     * will be selected as the [NavigationAnimationTransition] to use for animating the instructions.
     *
     * The transition lambdas will be ordered based on their priority, in descending order:
     * a transition lambda with priority 100 will be executed before one with priority 40).
     */
    @AdvancedEnroApi
    public fun addOpeningTransition(
        priority: Int,
        transition: (exiting: AnyOpenInstruction?, entering: AnyOpenInstruction) -> NavigationAnimationTransition?
    ) {
        opening.add(OpeningTransition(priority, transition))
    }

    /**
     * This is an Advanced Enro API. In most situations, [direction], [transitionTo] and
     * [transitionBetween] should provide the functionality required for standard navigation animations.
     *
     * [addClosingTransition]'s transition is defined by a lambda which returns a nullable
     * [NavigationAnimationTransition]. A [NavigationAnimationTransition] defines the [NavigationAnimation]
     * to use for both the entering and exiting instruction.
     *
     * When a NavigationInstruction is opened, all the transition lambdas registered in the relevant
     * contexts will be iterated through, passing in the instruction that is exiting (which will always
     * exist when an instruction is closed) and the instruction that is entering (if there is one).
     * The first of these lambdas to return a non-null [NavigationAnimationTransition]
     * will be selected as the [NavigationAnimationTransition] to use for animating the instructions.
     *
     * The transition lambdas will be ordered based on their priority, in descending order:
     * a transition lambda with priority 100 will be executed before one with priority 40).
     */
    @AdvancedEnroApi
    public fun addClosingTransition(
        priority: Int,
        transition: (exiting: AnyOpenInstruction, entering: AnyOpenInstruction?) -> NavigationAnimationTransition?
    ) {
        closing.add(ClosingTransition(priority, transition))
    }

    /**
     * Configures the animations for any instruction opened with a specified [NavigationDirection].
     *
     * Of all the transitions, this is the lowest priority; both [transitionTo] and [transitionBetween]
     * will take precedence over [direction].
     *
     * @param direction the direction that transitions are being configured for
     * @param entering the enter animation to use for the opening transition
     * @param exiting the exit animation to use for the opening transition
     * @param returnEntering the enter animation to use for the return/close transition, defaults to [entering]
     * @param returnExiting the exit animation to use for the return/close transition, defaults to [exiting]
     *
     * If either [returnEntering] or [returnExiting] are null, the return/close transition will not be overridden
     */
    public fun direction(
        direction: NavigationDirection,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter? = entering,
        returnExiting: NavigationAnimation.Exit? = exiting,
    ) {
        addOpeningTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }

        if (returnEntering == null) return
        if (returnExiting == null) return
        addClosingTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction == null) return@addClosingTransition null
            if (enteringInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            NavigationAnimationTransition(
                entering = returnEntering,
                exiting = returnExiting,
            )
        }
    }

    /**
     * Configures the animations for when a [NavigationInstruction] with a [NavigationKey] of type [Key]
     * is being opened or closed.
     *
     * This transition type takes precedence over [direction], but is of lower priority than
     * [transitionBetween]; if there is a [transitionBetween] that involves [Key], that transition
     * will take precedence.
     *
     * @param direction an optional direction that this transition will be configured for
     * @param entering the enter animation to use for the opening transition
     * @param exiting the exit animation to use for the opening transition
     * @param returnEntering the enter animation to use for the return/close transition, defaults to [entering]
     * @param returnExiting the exit animation to use for the return/close transition, defaults to [exiting]
     *
     * If either [returnEntering] or [returnExiting] are null, the return/close transition will not be overridden
     */
    public inline fun <reified Key : NavigationKey> transitionTo(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter? = entering,
        returnExiting: NavigationAnimation.Exit? = exiting,
    ) {
        addOpeningTransition(PARTIAL_KEY_PRIORITY) { _, enteringInstruction ->
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Key) return@addOpeningTransition null
            NavigationAnimationTransition(
                entering = entering,
                exiting = exiting,
            )
        }

        if (returnEntering == null) return
        if (returnExiting == null) return
        addClosingTransition(PARTIAL_KEY_PRIORITY) { exitingInstruction, _ ->
            if (direction != null && exitingInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            if (exitingInstruction.navigationKey !is Key) return@addClosingTransition null
            NavigationAnimationTransition(
                entering = returnEntering,
                exiting = returnExiting,
            )
        }
    }

    /**
     * Configures the animations for when a screen with a [NavigationInstruction] containing a
     * NavigationKey of type [Exit] opens a [NavigationInstruction] with a
     * [NavigationKey] of type [Enter], or when performing the reverse; a [NavigationInstruction] with
     * type [Enter] is closed and will make visible a [NavigationInstruction] with a [NavigationKey] of
     * type [Exit]
     *
     * This transition type is the highest priority, as it is the most specific. It will take precedence
     * over both [direction] and [transitionTo].
     *
     * @param direction an optional direction that this transition will be configured for
     * @param entering the enter animation to use for the opening transition
     * @param exiting the exit animation to use for the opening transition
     * @param returnEntering the enter animation to use for the return/close transition, defaults to [entering]
     * @param returnExiting the exit animation to use for the return/close transition, defaults to [exiting]
     *
     * If either [returnEntering] or [returnExiting] are null, the return/close transition will not be overridden
     */
    public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> transitionBetween(
        direction: NavigationDirection? = null,
        entering: NavigationAnimation.Enter,
        exiting: NavigationAnimation.Exit,
        returnEntering: NavigationAnimation.Enter? = entering,
        returnExiting: NavigationAnimation.Exit? = exiting,
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

        if (returnEntering == null) return
        if (returnExiting == null) return
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
        @AdvancedEnroApi
        public const val DEFAULT_PRIORITY: Int = 0
        @AdvancedEnroApi
        public const val DIRECTION_PRIORITY: Int = 10
        @AdvancedEnroApi
        public const val PARTIAL_KEY_PRIORITY: Int = 20
        @AdvancedEnroApi
        public const val EXACT_KEY_PRIORITY: Int = 30
    }
}

//// Resource extensions
///**
// * An overload of [NavigationAnimationOverrideBuilder.transitionBetween] that allows providing
// * Anim or Animator resources
// *
// * @see [NavigationAnimationOverrideBuilder.direction]
// */
//public fun NavigationAnimationOverrideBuilder.direction(
//    direction: NavigationDirection,
//    @AnimRes @AnimatorRes entering: Int,
//    @AnimRes @AnimatorRes exiting: Int,
//    @AnimRes @AnimatorRes returnEntering: Int? = entering,
//    @AnimRes @AnimatorRes returnExiting: Int? = exiting,
//) {
//    direction(
//        direction = direction,
//        entering = NavigationAnimation.Resource(entering),
//        exiting = NavigationAnimation.Resource(exiting),
//        returnEntering = returnEntering?.let { NavigationAnimation.Resource(it) },
//        returnExiting = returnExiting?.let { NavigationAnimation.Resource(it) },
//    )
//}
//
///**
// * An overload of [NavigationAnimationOverrideBuilder.transitionBetween] that allows providing
// * Anim or Animator resources
// *
// * @see [NavigationAnimationOverrideBuilder.transitionTo]
// */
//public inline fun <reified Key : NavigationKey> NavigationAnimationOverrideBuilder.transitionTo(
//    direction: NavigationDirection? = null,
//    @AnimRes @AnimatorRes entering: Int,
//    @AnimRes @AnimatorRes exiting: Int,
//    @AnimRes @AnimatorRes returnEntering: Int? = entering,
//    @AnimRes @AnimatorRes returnExiting: Int? = exiting,
//) {
//    transitionTo<Key>(
//        direction = direction,
//        entering = NavigationAnimation.Resource(entering),
//        exiting = NavigationAnimation.Resource(exiting),
//        returnEntering = returnEntering?.let { NavigationAnimation.Resource(it) },
//        returnExiting = returnExiting?.let { NavigationAnimation.Resource(it) },
//    )
//}
//
///**
// * An overload of [NavigationAnimationOverrideBuilder.transitionBetween] that allows providing
// * Anim or Animator resources
// *
// * @see [NavigationAnimationOverrideBuilder.transitionBetween]
// */
//public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> NavigationAnimationOverrideBuilder.transitionBetween(
//    direction: NavigationDirection? = null,
//    @AnimRes @AnimatorRes entering: Int,
//    @AnimRes @AnimatorRes exiting: Int,
//    @AnimRes @AnimatorRes returnEntering: Int? = entering,
//    @AnimRes @AnimatorRes returnExiting: Int? = exiting,
//) {
//    transitionBetween<Exit, Enter>(
//        direction = direction,
//        entering = NavigationAnimation.Resource(entering),
//        exiting = NavigationAnimation.Resource(exiting),
//        returnEntering = returnEntering?.let { NavigationAnimation.Resource(it) },
//        returnExiting = returnExiting?.let { NavigationAnimation.Resource(it) },
//    )
//}

// Composable transition extensions

/**
 * An overload of [NavigationAnimationOverrideBuilder.direction] that allows providing Composable animations
 *
 * @see [NavigationAnimationOverrideBuilder.direction]
 */
public fun NavigationAnimationOverrideBuilder.direction(
    direction: NavigationDirection,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition? = entering,
    returnExiting: ExitTransition? = exiting,
) {
    direction(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
        returnEntering = returnEntering?.let { NavigationAnimation.Composable(it) },
        returnExiting = returnExiting?.let { NavigationAnimation.Composable(it) },
    )
}

/**
 * An overload of [NavigationAnimationOverrideBuilder.transitionTo] that allows providing Composable animations
 *
 * @see [NavigationAnimationOverrideBuilder.transitionTo]
 */
public inline fun <reified Key : NavigationKey> NavigationAnimationOverrideBuilder.transitionTo(
    direction: NavigationDirection? = null,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition? = entering,
    returnExiting: ExitTransition? = exiting,
) {
    transitionTo<Key>(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
        returnEntering = returnEntering?.let { NavigationAnimation.Composable(it) },
        returnExiting = returnExiting?.let { NavigationAnimation.Composable(it) },
    )
}

/**
 * An overload of [NavigationAnimationOverrideBuilder.transitionBetween] that allows providing Composable animations
 *
 * @see [NavigationAnimationOverrideBuilder.transitionBetween]
 */
public inline fun <reified Exit : NavigationKey, reified Enter : NavigationKey> NavigationAnimationOverrideBuilder.transitionBetween(
    direction: NavigationDirection? = null,
    entering: EnterTransition,
    exiting: ExitTransition,
    returnEntering: EnterTransition? = entering,
    returnExiting: ExitTransition? = exiting,
) {
    transitionBetween<Exit, Enter>(
        direction = direction,
        entering = NavigationAnimation.Composable(entering),
        exiting = NavigationAnimation.Composable(exiting),
        returnEntering = returnEntering?.let { NavigationAnimation.Composable(it) },
        returnExiting = returnExiting?.let { NavigationAnimation.Composable(it) },
    )
}