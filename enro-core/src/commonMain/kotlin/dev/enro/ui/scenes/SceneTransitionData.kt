package dev.enro.ui.scenes

import dev.enro.NavigationContainer
import dev.enro.NavigationKey

/**
 * Provides transition data for navigation animations in a NavigationDisplay.
 * 
 * This interface is used as the transition data type in AnimatedContentTransitionScope when defining
 * navigation animations. It allows animation definitions to access information about the current
 * navigation state and make decisions based on what content is visible.
 * 
 * When defining animations in NavigationAnimations, you can access this data through the
 * AnimatedContentTransitionScope's initialState and targetState properties. This enables creating
 * context-aware animations based on the navigation state.
 * 
 * Example usage:
 * ```
 * transitionSpec = {
 *     // Different animation when content first becomes visible
 *     if (initialState.visible.isEmpty() && targetState.visible.isNotEmpty()) {
 *         // First appearance animation
 *         ContentTransform(
 *             targetContentEnter = fadeIn() + scaleIn(),
 *             initialContentExit = fadeOut()
 *         )
 *     } else {
 *         // Regular navigation animation
 *         ContentTransform(
 *             targetContentEnter = slideInHorizontally { it },
 *             initialContentExit = slideOutHorizontally { -it }
 *         )
 *     }
 * }
 * ```
 */
public interface SceneTransitionData {
    /**
     * The key of the navigation container that this scene belongs to.
     * This can be used to determine if a transition is happening between different containers.
     */
    public val containerKey: NavigationContainer.Key
    
    /**
     * List of navigation key instances that are currently visible in this scene.
     * This can be used to create different animations based on what content is visible,
     * such as detecting when a scene is empty (first destination) or has multiple items.
     */
    public val visible: List<NavigationKey.Instance<NavigationKey>>
}