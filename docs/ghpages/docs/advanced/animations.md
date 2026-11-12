---
title: Animations
parent: Advanced Topics
nav_order: 3
---

# Animations

`NavigationDisplay` animates the transition between scenes. A
`NavigationAnimations` value declares the enter and exit transitions.
Inside a destination, `Modifier.animateNavigationEnterExit` and
`NavigationAnimatedVisibility` animate one element with separate timing
from the destination transition.

## Backgrounds and transitions

> Give each animated destination an **opaque background**. A Compose
> destination is transparent unless the destination draws a background.

During a transition, `NavigationDisplay` composes the outgoing destination
and the incoming destination at the same time. When either destination is
transparent, the content of one destination is visible through the other
destination.

Wrap each destination that is not an overlay in a `Surface`, or another
opaque container, with the theme's background colour.

**Per destination** — add the `Surface` at the root of the destination:

<!-- snippet: animations/background-per-destination -->
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

**For every destination** — add a `decorator` to the `NavigationComponent`
module. The recipes app uses a decorator in
[`RecipesComponent`][recipes-component]:

<!-- snippet: animations/background-decorator -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        decorator {
            navigationDestinationDecorator { destination ->
                val isOverlay = destination.isDirectOverlay() ||
                    destination.metadata.contains(DialogSceneStrategy.DialogPropertiesKey)
                if (isOverlay) {
                    destination.Content()
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        destination.Content()
                    }
                }
            }
        }
    }
)
```

The decorator leaves dialogs and direct overlays transparent, because a
dialog or direct overlay is drawn over another destination.

## NavigationAnimations

`NavigationAnimations` contains the transition specs for a
`NavigationDisplay`. `transitionSpec` applies to forward navigation and
`popTransitionSpec` applies to a pop:

<!-- snippet: animations/navigation-animations -->
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

Each transition spec has an `AnimatedContentTransitionScope` receiver, the
same receiver as the `transitionSpec` of Compose's `AnimatedContent`.

The `animations` parameter of `NavigationDisplay` defaults to
`NavigationAnimations.Default`. The [animated navigation
recipe][animations-recipe] switches between horizontal slide, vertical
slide, scale with fade, and no animation.

## Per-element animations inside a destination

The `NavigationDisplay` transition animates a whole destination. The two
functions in `dev.enro.ui.animation` animate one element of a destination
with separate timing. For example, a dialog scrim can fade while the dialog
card slides.

### `Modifier.animateNavigationEnterExit`

`Modifier.animateNavigationEnterExit` adds an enter transition and an exit
transition to one element. The enter transition runs when the destination
enters and the exit transition runs when the destination exits.

<!-- snippet: animations/enter-exit-modifier -->
```kotlin
Box(
    Modifier
        .matchParentSize()
        .animateNavigationEnterExit(
            enter = fadeIn(tween(durationMillis = 320)),
            exit = fadeOut(tween(durationMillis = 220)),
        )
        .background(Color.Black.copy(alpha = 0.5f)),
)
```

Outside a `NavigationDisplay`, such as in a snapshot test,
`Modifier.animateNavigationEnterExit` does nothing.

### `NavigationAnimatedVisibility`

`NavigationAnimatedVisibility` wraps content in an `AnimatedVisibility`
that is visible while the destination is visible. The `enter` and `exit`
arguments set the delay and the curve of the content:

<!-- snippet: animations/animated-visibility -->
```kotlin
NavigationAnimatedVisibility(
    enter = fadeIn(tween(durationMillis = 220, delayMillis = 140)),
    exit = fadeOut(tween(durationMillis = 100)),
) {
    Row { /* action buttons that come in slightly later than the rest */ }
}
```

Outside a `NavigationDisplay`, `NavigationAnimatedVisibility` shows the
content without an animation.

## Choosing an animation API

- **Transition for the whole destination** → `NavigationDisplay(animations = ...)`.
- **Enter and exit transition for one element** → `Modifier.animateNavigationEnterExit`.
- **Content that appears after a delay** → `NavigationAnimatedVisibility`.

The [staggered animations recipe][staggered-recipe] uses all three APIs: a
`NavigationDisplay` transition for the screen, and per-element animations
for the dialog scrim, the card and the buttons.

## Predictive back

`NavigationDisplay` handles the predictive back gesture. During the
gesture, `NavigationDisplay` runs
`NavigationAnimations.predictivePopTransitionSpec` and sets the transition
progress from the gesture progress.

The `predictivePopTransitionSpec` constructor parameter defaults to a call
to `popTransitionSpec`. `NavigationAnimations.Default` declares a separate
`predictivePopTransitionSpec`, so
`NavigationAnimations.Default.copy(popTransitionSpec = ...)` does not change
the gesture transition.

`predictivePopTransitionSpec` receives the edge where the swipe started, as
a `NavigationEvent.SwipeEdge` value. The transition can differ for left and
right swipes:

<!-- snippet: animations/predictive-back -->
```kotlin
NavigationDisplay(
    state = container,
    animations = NavigationAnimations.Default.copy(
        predictivePopTransitionSpec = { swipeEdge ->
            ContentTransform(
                targetContentEnter = fadeIn(),
                initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
            )
        },
    ),
)
```

A scene overrides the predictive back spec with the
`NavigationDisplay.PredictivePopTransitionKey` scene metadata key. A scene
overrides the forward spec with `NavigationDisplay.TransitionKey` and the
pop spec with `NavigationDisplay.PopTransitionKey`.

## Shared element animations

A Compose shared element transition animates the bounds and content of two
matching elements when the user navigates between destinations. For
example, a list thumbnail animates into the image on the detail screen.
`NavigationDisplay` wraps the destinations in a `SharedTransitionLayout`,
unless the `sharedTransitionScope` argument is set, and provides two
CompositionLocals in `dev.enro.ui`:

| CompositionLocal | Type | Use |
|---|---|---|
| `LocalNavigationSharedTransitionScope` | `SharedTransitionScope` | The receiver for `Modifier.sharedElement`, `Modifier.sharedBounds` and `rememberSharedContentState`. |
| `LocalNavigationAnimatedVisibilityScope` | The `AnimatedVisibilityScope` of the destination's enter and exit transition | The `animatedVisibilityScope` argument of `sharedElement`. |

To share an element between two destinations:

1. Choose a stable key for the element.
2. In the **source** destination, add `sharedElement` with the key to the element.
3. In the **target** destination, add `sharedElement` with the same key to the matching element.

<!-- snippet: animations/shared-elements -->
```kotlin
@Composable
@NavigationDestination(ItemList::class)
fun ItemListDestination() {
    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current

    LazyColumn {
        items(myItems) { item ->
            with(sharedTransitionScope) {
                // The item's thumbnail.
                Box(
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState(key = "thumb-${item.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .size(48.dp),
                )
            }
        }
    }
}

@Composable
@NavigationDestination(ItemDetail::class)
fun ItemDetailDestination() {
    val navigation = navigationHandle<ItemDetail>()
    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current

    with(sharedTransitionScope) {
        // The same thumbnail, larger.
        Box(
            modifier = Modifier
                .sharedElement(
                    rememberSharedContentState(key = "thumb-${navigation.key.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .size(160.dp),
        )
    }
}
```

When `ItemDetail` opens, Compose animates the bounds and content of the
`ItemList` element with the key `"thumb-${item.id}"` into the `ItemDetail`
element with the same key. When the user navigates back, Compose runs the
animation in reverse.

### Shared element keys

The key in the source destination must equal the key in the target
destination. Build the key from a stable value such as the item's id. When
one item has several shared elements, such as a thumbnail and a title,
include the element type in the key:

<!-- snippet: animations/shared-element-keys -->
```kotlin
private fun thumbKey(id: Int) = "thumb-$id"
private fun titleKey(id: Int) = "title-$id"
```

A key must have the same value on every recomposition. Do not build a key
from a remembered value that can change.

### Shared elements in `navigationDestination { }`

The content lambda of `navigationDestination<Key> { ... }` has a
`NavigationDestinationScope<T>` receiver, which implements
`SharedTransitionScope` and `AnimatedVisibilityScope`. Call `sharedElement`,
`sharedBounds` and `rememberSharedContentState` on the receiver, and pass
`this@navigationDestination` as the `animatedVisibilityScope`. The
`NavigationKey` is `navigation.key`:

<!-- snippet: animations/shared-elements-provider -->
```kotlin
@NavigationDestination(ItemDetail::class)
val itemDetailDestination = navigationDestination<ItemDetail> {
    Box(
        modifier = Modifier.sharedElement(
            rememberSharedContentState(key = thumbKey(navigation.key.id)),
            animatedVisibilityScope = this@navigationDestination,
        ),
    )
}
```

The provider form does not read the two CompositionLocals in the
destination.

### Other `SharedTransitionScope` APIs

The other `SharedTransitionScope` APIs take the same two scopes:
`Modifier.sharedBounds` for elements with different content, a
`BoundsTransform` argument, and `OverlayClip` configuration.

### Shared elements that do not animate

- **The keys differ.** Check that the source key and the target key are
  equal. A key built from `id.hashCode()` does not equal a key built from
  `id`.
- **The two destinations were not composed together.** A shared element
  transition requires the source destination and the target destination in
  the same transition. Opening the target on top of the source meets the
  requirement.
- **One destination is rendered outside `NavigationDisplay`.**
  `NavigationDisplay` provides the `SharedTransitionLayout`. Content
  rendered outside the `NavigationDisplay` is not inside the
  `SharedTransitionLayout`.

The [shared elements recipe][sharedelements-recipe] is a runnable list and
detail example.

## See also

- [Animated navigation recipe][animations-recipe] — switches between
  several transition styles.
- [Staggered animations recipe][staggered-recipe] —
  `Modifier.animateNavigationEnterExit` and `NavigationAnimatedVisibility`
  in an overlay and in a full-screen destination.
- [Shared elements recipe][sharedelements-recipe] — list and detail screens
  with a shared icon and title.
- [Navigation Destinations → Custom enter/exit on overlays](../core-concepts/navigation-destinations.md#custom-enterexit-on-overlays).

[animations-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/animations/AnimatedNavigation.kt
[staggered-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/animations/StaggeredAnimations.kt
[sharedelements-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/sharedelements/SharedElementAnimations.kt
[recipes-component]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/RecipesComponent.kt
