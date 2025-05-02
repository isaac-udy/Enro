package dev.enro.animation

import dev.enro.core.AnyOpenInstruction
import kotlin.reflect.KClass

internal data class NavigationAnimationOverride(
    val parent: NavigationAnimationOverride?,
    val defaults: Map<KClass<out NavigationAnimation>, NavigationAnimation.Defaults<out NavigationAnimation>>,
    val opening: List<OpeningTransition>,
    val closing: List<ClosingTransition>,
) {
}

/**
 * This function is used to find the default animations for a specific type of NavigationAnimation (T),
 * and will start at the current NavigationAnimationOverride instance, but then check for a default
 * animation configuration in the parent NavigationAnimationOverride instance, recursively all the
 * way up to the root.
 *
 * If there is no default animation configuration found for a type, an error is thrown.
 */
internal fun <T : NavigationAnimation> NavigationAnimationOverride.findDefaults(type: KClass<T>) : NavigationAnimation.Defaults<T> {
    val defaults = defaults[type] as? NavigationAnimation.Defaults<T>
    if (defaults != null) {
        return defaults
    }
    val parent = parent
    when (parent) {
        null -> error("No default animations found for type ${type.simpleName}")
        else -> return parent.findDefaults(type)
    }
}

/**
 * This function is used to find the override for a specific type of NavigationAnimation (T) for
 * opening animations. It will start at the current NavigationAnimationOverride instance, but then
 * check for an override in the parent NavigationAnimationOverride instance, recursively all the way
 * up to the root.
 *
 * If there is no override found for a type, null is returned.
 */
internal fun <T : NavigationAnimation> NavigationAnimationOverride.findOverrideForOpening(
    type: KClass<T>,
    exiting: AnyOpenInstruction?,
    entering: AnyOpenInstruction,
): T? {
    val opening = mutableMapOf<Int, MutableList<OpeningTransition>>()
    var override: NavigationAnimationOverride? = this
    while(override != null) {
        override.opening.reversed().forEach {
            opening.getOrPut(it.priority) { mutableListOf() }
                .add(it)
        }
        override = override.parent
    }
    opening.keys.sortedDescending()
        .flatMap { opening[it].orEmpty() }
        .forEach {
            val animation = it.transition(exiting, entering)
                ?: return@forEach
            if (animation::class != type) return@forEach
            return animation as? T
        }
    return null
}

/**
 * This function is used to find the override for a specific type of NavigationAnimation (T) for
 * closing animations. It will start at the current NavigationAnimationOverride instance, but then
 * check for an override in the parent NavigationAnimationOverride instance, recursively all the way
 * up to the root.
 *
 * If there is no override found for a type, null is returned.
 */
internal fun <T: NavigationAnimation> NavigationAnimationOverride.findOverrideForClosing(
    type: KClass<T>,
    exiting: AnyOpenInstruction,
    entering: AnyOpenInstruction?,
): T? {
    val closing = mutableMapOf<Int, MutableList<ClosingTransition>>()
    var override: NavigationAnimationOverride? = this
    while(override != null) {
        override.closing.reversed().forEach {
            closing.getOrPut(it.priority) { mutableListOf() }
                .add(it)
        }
        override = override.parent
    }
    closing.keys.sortedDescending()
        .flatMap { closing[it].orEmpty() }
        .forEach {
            val animation = it.transition(exiting, entering)
                ?: return@forEach
            if (animation::class != type) return@forEach
            return animation as? T
        }
    return null
}