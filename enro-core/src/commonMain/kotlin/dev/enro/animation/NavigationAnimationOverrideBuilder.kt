package dev.enro.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.originalNavigationDirection
import kotlin.reflect.KClass


public class NavigationAnimationOverrideBuilder {
    private val defaults =
        mutableMapOf<KClass<out NavigationAnimation>, NavigationAnimation.Defaults<out NavigationAnimation>>()
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
        transition: (exiting: AnyOpenInstruction?, entering: AnyOpenInstruction) -> NavigationAnimation?
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
        transition: (exiting: AnyOpenInstruction, entering: AnyOpenInstruction?) -> NavigationAnimation?
    ) {
        closing.add(ClosingTransition(priority, transition))
    }

    public fun <T : NavigationAnimation> defaults(
        type: KClass<T>,
        defaults: NavigationAnimation.Defaults<T>,
    ) {
        this.defaults[type] = defaults
    }

    public inline fun <reified T : NavigationAnimation> defaults(
        defaults: NavigationAnimation.Defaults<T>
    ) {
        defaults(T::class, defaults)
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
        animation: NavigationAnimation,
        returnAnimation: NavigationAnimation? = null,
    ) {
        addOpeningTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            animation
        }

        if (returnAnimation == null) return
        addClosingTransition(DIRECTION_PRIORITY) { _, enteringInstruction ->
            if (enteringInstruction == null) return@addClosingTransition null
            if (enteringInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            returnAnimation
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
        animation: NavigationAnimation,
        returnAnimation: NavigationAnimation? = null,
    ) {
        addOpeningTransition(PARTIAL_KEY_PRIORITY) { _, enteringInstruction ->
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Key) return@addOpeningTransition null
            animation
        }

        addClosingTransition(PARTIAL_KEY_PRIORITY) { exitingInstruction, _ ->
            if (direction != null && exitingInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            if (exitingInstruction.navigationKey !is Key) return@addClosingTransition null
            returnAnimation
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
        animation: NavigationAnimation,
        returnAnimation: NavigationAnimation? = null,
    ) {
        addOpeningTransition(EXACT_KEY_PRIORITY) { exitingInstruction, enteringInstruction ->
            if (exitingInstruction == null) return@addOpeningTransition null
            if (direction != null && enteringInstruction.originalNavigationDirection() != direction) return@addOpeningTransition null
            if (exitingInstruction.navigationKey !is Exit) return@addOpeningTransition null
            if (enteringInstruction.navigationKey !is Enter) return@addOpeningTransition null
            animation
        }

        if (returnAnimation == null) return
        addClosingTransition(EXACT_KEY_PRIORITY) { exitingInstruction, enteringInstruction ->
            if (enteringInstruction == null) return@addClosingTransition null
            if (direction != null && exitingInstruction.originalNavigationDirection() != direction) return@addClosingTransition null
            if (exitingInstruction.navigationKey !is Enter) return@addClosingTransition null
            if (enteringInstruction.navigationKey !is Exit) return@addClosingTransition null
            returnAnimation
        }
    }

    internal fun build(parent: NavigationAnimationOverride?): NavigationAnimationOverride {
        return NavigationAnimationOverride(
            parent = parent,
            defaults = defaults,
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
    val returnAnimation = if (returnEntering != null || returnExiting != null) {
        NavigationAnimationForComposable(
            enter = returnEntering ?: entering,
            exit = returnExiting ?: exiting,
        )
    } else {
        null
    }
    direction(
        direction = direction,
        animation = NavigationAnimationForComposable(
            entering,
            exiting,
        ),
        returnAnimation = returnAnimation,
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
    val returnAnimation = if (returnEntering != null || returnExiting != null) {
        NavigationAnimationForComposable(
            enter = returnEntering ?: entering,
            exit = returnExiting ?: exiting,
        )
    } else {
        null
    }
    transitionTo<Key>(
        direction = direction,
        animation = NavigationAnimationForComposable(
            entering,
            exiting,
        ),
        returnAnimation = returnAnimation,
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
    val returnAnimation = if (returnEntering != null || returnExiting != null) {
        NavigationAnimationForComposable(
            enter = returnEntering ?: entering,
            exit = returnExiting ?: exiting,
        )
    } else {
        null
    }
    transitionBetween<Exit, Enter>(
        direction = direction,
        animation = NavigationAnimationForComposable(
            enter = entering,
            exit = exiting,
        ),
        returnAnimation = returnAnimation,
    )
}