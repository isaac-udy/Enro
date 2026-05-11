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

## Backgrounds and transitions

> A destination that participates in animations should have an **opaque
> background**. Compose destinations are transparent by default — whatever
> is composed behind them shows through.

During a transition, two destinations are composed at once (the outgoing
one fading or sliding out, the incoming one fading or sliding in), and
any shared elements move over the top. If either destination is
transparent, the two overlap visibly and the result looks unpolished —
text on one screen reading "through" content on the other.

The fix is to wrap each non-overlay destination in a `Surface` (or any
opaque container) coloured with your theme's background. Two ways to do
it:

**Per destination** — apply the background at the destination's root:

```kotlin
@Composable
@NavigationDestination(MyScreen::class)
fun MyScreenDestination() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        // ...
    }
}
```

**Globally** — add a `decorator` on your `NavigationComponent` so every
destination is wrapped automatically. This is what the recipes app does
(see [`RecipesComponent`][recipes-component]):

```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        decorator {
            navigationDestinationDecorator { destination ->
                if (destination.isDirectOverlay() /* … and not a dialog */) {
                    destination.content()
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        destination.content()
                    }
                }
            }
        }
    }
)
```

Skip dialogs and direct overlays — they're intentionally drawn over the
top of another destination and need to stay transparent.

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

## Shared element animations

Compose's shared-element transitions animate the bounds and contents of a
matched pair of elements as the user navigates between destinations — the
list-item thumbnail "grows" into the detail screen's hero image, for
example. Enro wires the necessary scopes for you: `NavigationDisplay`
wraps its rendered destinations in a `SharedTransitionLayout` and exposes
the two CompositionLocals every shared element needs.

The two CompositionLocals live in `dev.enro.ui`:

| Local | What it is | What it's for |
|---|---|---|
| `LocalNavigationSharedTransitionScope` | A `SharedTransitionScope` | Hosts `Modifier.sharedElement`, `Modifier.sharedBounds`, `rememberSharedContentState`, etc. Read it once per destination. |
| `LocalNavigationAnimatedVisibilityScope` | The `AnimatedVisibilityScope` for the destination's enter/exit | Passed to `sharedElement` so it knows when this side of the transition is appearing or disappearing. |

The pattern is always the same:

1. Pick a stable key for each element you want to share.
2. On the **source** destination, tag the element with that key.
3. On the **target** destination, tag the matching element with the same key.

```kotlin
@Composable
@NavigationDestination(ItemList::class)
fun ItemListDestination() {
    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current

    LazyColumn { items(myItems) { item ->
        with(sharedTransitionScope) {
            Image(
                modifier = Modifier
                    .sharedElement(
                        rememberSharedContentState(key = "thumb-${item.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .size(48.dp),
                // ...
            )
        }
    }}
}

@Composable
@NavigationDestination(ItemDetail::class)
fun ItemDetailDestination() {
    val navigation = navigationHandle<ItemDetail>()
    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current

    with(sharedTransitionScope) {
        Image(
            modifier = Modifier
                .sharedElement(
                    rememberSharedContentState(key = "thumb-${navigation.key.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .size(160.dp),
            // ...
        )
    }
}
```

When `ItemDetail` opens, Compose finds the element on `ItemList` tagged
`"thumb-${item.id}"` and animates the bounds and contents into the matched
element on the detail screen. When the user navigates back, the same
animation plays in reverse.

### Picking keys

Keys must match between the source and the target. They can be anything
serializable to a stable string — the item's id is the usual choice.
Mixing in a content type avoids accidental collisions when several
sharable elements per item participate (a thumbnail and a title, say):

```kotlin
private fun thumbKey(id: Int) = "thumb-$id"
private fun titleKey(id: Int) = "title-$id"
```

Stable keys also means stable across recompositions — don't compute them
from a `remember`-ed value that could change.

### Inside the `navigationDestination { }` provider form

If you're using the provider-val style (`val foo =
navigationDestination<Key> { ... }`), the content lambda's receiver
`NavigationDestinationScope<T>` already implements both
`SharedTransitionScope` and `AnimatedVisibilityScope`. You can call
`sharedElement`, `sharedBounds`, and `rememberSharedContentState` directly
on the receiver and pass `this` (or `this@navigationDestination`) as the
`animatedVisibilityScope`:

```kotlin
@NavigationDestination(ItemDetail::class)
val itemDetailDestination = navigationDestination<ItemDetail> {
    Image(
        modifier = Modifier.sharedElement(
            rememberSharedContentState(key = thumbKey(key.id)),
            animatedVisibilityScope = this@navigationDestination,
        ),
        // ...
    )
}
```

Useful when the destination is short and you want to avoid the two
`Local*` reads at the top.

### Beyond `sharedElement`

Anything Compose's `SharedTransitionScope` supports works inside an Enro
destination — `Modifier.sharedBounds` (for elements whose contents differ
but whose bounds should animate), the `SharedContentState` overload that
takes a `BoundsTransform`, manual `OverlayClip` configuration, etc. You
won't need any Enro-specific plumbing past reading the two CompositionLocals.

### When shared elements don't fire

A few common gotchas, in order of likelihood:

- **Key mismatch.** Double-check the key strings produce the same value on
  both sides — a stray space or a type mismatch (`"$id"` vs
  `"${id.toString()}"` is fine; `id.hashCode()` vs `id` is not).
- **Both destinations didn't render together.** Shared transitions need
  the source destination to still be on the backstack while the target
  is composing. If you replace the source instead of pushing on top of it,
  there's nothing to animate from.
- **One side is rendered outside `NavigationDisplay`.** Only destinations
  rendered by `NavigationDisplay` get the surrounding
  `SharedTransitionLayout`. Embedded subscreens or popups that bypass it
  won't participate.

A full runnable example lives in the [shared-elements
recipe][sharedelements-recipe], with a list-to-detail flow over a few
sample items.

## See also

- [Animated navigation recipe][animations-recipe] — switching between
  several transition styles.
- [Staggered animations recipe][staggered-recipe] —
  `Modifier.animateNavigationEnterExit` + `NavigationAnimatedVisibility`
  combined inside both an overlay and a regular pushed destination.
- [Shared elements recipe][sharedelements-recipe] — list-to-detail with
  shared icon and title.
- [Navigation Destinations → Custom enter/exit on overlays](../core-concepts/navigation-destinations.md#custom-enterexit-on-overlays).

[animations-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/animations/AnimatedNavigation.kt
[staggered-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/animations/StaggeredAnimations.kt
[sharedelements-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/sharedelements/SharedElementAnimations.kt
[recipes-component]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/RecipesComponent.kt
