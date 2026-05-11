---
title: Animations
parent: Advanced Topics
nav_order: 3
---

# Animations

Enro animates navigation transitions through `NavigationDisplay`, using a
`NavigationAnimations` data class to describe the enter/exit transitions
between scenes. Inside a destination, per-element animation primitives
(`Modifier.animateNavigationEnterExit`, `NavigationAnimatedVisibility`) let
you animate parts of a screen on their own timing, alongside the
destination-level transition.

## NavigationAnimations

A `NavigationAnimations` is a pair of transition specs — one for forward
navigation, one for pop:

```kotlin
NavigationDisplay(
    state = container,
    animations = NavigationAnimations(
        transitionSpec = {
            ContentTransform(
                targetContentEnter = slideInHorizontally { it } + fadeIn(tween(200)),
                initialContentExit = slideOutHorizontally { -it / 3 } + fadeOut(tween(150)),
            )
        },
        popTransitionSpec = {
            ContentTransform(
                targetContentEnter = slideInHorizontally { -it / 3 } + fadeIn(tween(200)),
                initialContentExit = slideOutHorizontally { it } + fadeOut(tween(150)),
            )
        },
    ),
)
```

The transition specs run in an `AnimatedContentTransitionScope`, so you have
access to everything Compose's `AnimatedContent` API offers.

`NavigationAnimations.Default` is the default, and is what you'll get if
you don't pass an `animations` argument. The [animated navigation
recipe][animations-recipe] has a toggle that switches between several
common styles (horizontal slide, vertical slide, scale + fade, none).

## Per-element animations inside a destination

The destination-level transition animates the whole destination as a single
unit. To animate parts of a destination on their own timing — for example,
to fade a dialog scrim while sliding the card on a different curve — use
the two helpers in `dev.enro.ui.animation`.

### `Modifier.animateNavigationEnterExit`

The simplest hook. Attach it to any element that should have its own enter
and exit transitions, tied to the destination's enter/exit.

```kotlin
Box(
    Modifier
        .matchParentSize()
        .animateNavigationEnterExit(
            enter = fadeIn(tween(durationMillis = 320)),
            exit  = fadeOut(tween(durationMillis = 220)),
        )
        .background(Color.Black.copy(alpha = 0.5f)),
)
```

The element fades in when the destination enters and out when the
destination leaves, on whatever curve you specify.

### `NavigationAnimatedVisibility`

When you want a child block to actually appear or disappear in step with
the destination's enter/exit — with its own delay, its own curve, or both —
use `NavigationAnimatedVisibility`:

```kotlin
NavigationAnimatedVisibility(
    enter = fadeIn(tween(durationMillis = 220, delayMillis = 140)),
    exit  = fadeOut(tween(durationMillis = 100)),
) {
    Row { /* action buttons that come in slightly later than the rest */ }
}
```

This is the right tool when you want a staggered reveal of inner content
that respects the destination's overall lifecycle.

## When to use which

- **Whole-screen transition** → `NavigationDisplay(animations = ...)`.
- **One element on its own timing, no delay tricks** → `Modifier.animateNavigationEnterExit`.
- **Delayed or staggered reveal of inner content** → `NavigationAnimatedVisibility`.

A worked example combining all three (animated container transition for the
screen, then per-element animations for the dialog scrim, card, and
buttons) lives in the [staggered animations recipe][staggered-recipe].

## Predictive back

`NavigationDisplay` participates in Android's predictive back gesture
automatically — when the user starts a back gesture, the popTransitionSpec
plays in reverse, tracking the gesture progress. You don't need to
configure anything for the default behaviour; the predictive-back
animations the user sees are the same `popTransitionSpec` you defined
running on a different driver.

## Shared elements

The `NavigationDestinationScope<T>` receiver inside `navigationDestination
{ }` implements `SharedTransitionScope` and `AnimatedVisibilityScope`, so
you can use Compose's shared-element APIs without any extra plumbing:

```kotlin
val Recipe.titleKey = "recipe-title-${id}"

Text(
    "Recipe title",
    modifier = Modifier.sharedElement(
        sharedContentState = rememberSharedContentState(key = recipe.titleKey),
        animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current,
    ),
)
```

See `recipes/compose/ComposeSharedElementTransitions.kt` in the tests
application for a worked example.

## See also

- [Animated navigation recipe][animations-recipe] — switching between
  several transition styles.
- [Staggered animations recipe][staggered-recipe] —
  `Modifier.animateNavigationEnterExit` + `NavigationAnimatedVisibility`
  combined inside both an overlay and a regular pushed destination.
- [Navigation Destinations → Custom enter/exit on overlays](../core-concepts/navigation-destinations.md#custom-enterexit-on-overlays).

[animations-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/animations/AnimatedNavigation.kt
[staggered-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/animations/StaggeredAnimations.kt
