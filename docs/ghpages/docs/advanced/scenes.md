---
title: Scenes
parent: Advanced Topics
nav_order: 13
---

# Scenes

A scene is the layout for the visible part of a backstack.
`NavigationDisplay` calls a `NavigationSceneStrategy` with the container's
destinations, and renders the scene that the strategy returns.

A destination selects a scene with destination metadata. The scene does not
change the backstack or the keys.

## The default strategies

`NavigationDisplay` uses three strategies unless the `sceneStrategy`
argument is set. `NavigationDisplay` renders the scene from the first
strategy that returns a scene.

| Strategy | Used when the top destination has | Renders |
|---|---|---|
| `DialogSceneStrategy` | `dialog()` metadata | The destination in a `Dialog`, over the scene below |
| `DirectOverlaySceneStrategy` | `directOverlay()` metadata | The destination over the scene below, with no window of its own |
| `SinglePaneSceneStrategy` | anything else | The top destination, full size |

## Dialogs

Add `dialog()` to the destination's metadata. `dialog()` takes optional
`DialogProperties`.

<!-- snippet: scenes/dialog -->
```kotlin
@NavigationDestination(ConfirmDeleteDialog::class)
val confirmDeleteDialogDestination: NavigationDestinationProvider<ConfirmDeleteDialog> = navigationDestination(
    metadata = {
        dialog()
    }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Delete ${navigation.key.itemName}?")
            Text("This action cannot be undone.")

            Button(onClick = { navigation.close() }) {
                Text("Delete")
            }
            TextButton(onClick = { navigation.close() }) {
                Text("Cancel")
            }
        }
    }
}
```

## Bottom sheets and other overlays

Add `directOverlay()` to the destination's metadata and draw the overlay in
the destination: a `ModalBottomSheet`, a popup, or another composable. The
destination below the overlay stays on screen.

Pass `directOverlay(enter, exit)` to animate the overlay. The exit
transition also runs when code outside the destination closes the
destination.

See the [Bottom Sheet recipe][bottomsheet-recipe].

## Write a scene strategy

A strategy reads destination metadata to find the destinations that use
the strategy. Define a `NavigationDestination.MetadataKey` and a function
that adds the key:

<!-- snippet: scenes/metadata -->
```kotlin
internal object IsTwoPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)

/**
 * Marks a destination for two-pane rendering. [TwoPaneSceneStrategy]
 * renders two destinations side by side only when both of the top two
 * backstack entries have this metadata.
 */
fun NavigationDestination.MetadataBuilder<*>.twoPane() {
    add(IsTwoPaneKey, true)
}

internal fun NavigationDestination<*>.isTwoPane(): Boolean =
    metadata[IsTwoPaneKey]
```

A destination uses the strategy with `metadata = { twoPane() }`.

`calculateScene` receives the container's destinations and returns a
`NavigationScene`. When `calculateScene` returns `null`, `NavigationDisplay`
calls the next strategy:

<!-- snippet: scenes/strategy -->
```kotlin
class TwoPaneSceneStrategy(
    private val widthBreakpointDp: Int = 600,
) : NavigationSceneStrategy {

    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        if (entries.size < 2) return null

        val width = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.toSize().width.toDp()
        }
        if (width < widthBreakpointDp.dp) return null

        val first = entries[entries.lastIndex - 1]
        val second = entries.last()
        if (!first.isTwoPane() || !second.isTwoPane()) return null

        return remember(first.instance.id, second.instance.id) {
            object : NavigationScene {
                override val entries: List<NavigationDestination<NavigationKey>> =
                    listOf(first, second)

                override val previousEntries: List<NavigationDestination<NavigationKey>> =
                    // previousEntries is the backstack after Back: Back from
                    // the two-pane scene removes the top entry only.
                    entries.dropLast(1)

                override val key: Any =
                    TwoPaneSceneStrategy::class to (first.instance.id to second.instance.id)

                override val content: @Composable (() -> Unit) = {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            first.Content()
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            second.Content()
                        }
                    }
                }
            }
        }
    }
}
```

A `NavigationScene` has four members:

| Member | Meaning |
|---|---|
| `key` | The scene's identity. `NavigationDisplay` animates between scenes with different keys. |
| `entries` | The destinations the scene shows. |
| `previousEntries` | The destinations left after going back from the scene. |
| `content` | The layout. Call `Content()` on a destination to draw the destination. |

Put the strategy before `SinglePaneSceneStrategy` in the chain:

<!-- snippet: scenes/display -->
```kotlin
NavigationDisplay(
    state = container,
    modifier = modifier,
    sceneStrategy = NavigationSceneStrategy.from(
        DialogSceneStrategy(),
        DirectOverlaySceneStrategy(),
        TwoPaneSceneStrategy(),
        SinglePaneSceneStrategy(),
    ),
)
```

### Back from inside a scene

`calculateScene` has a `SceneStrategyScope` receiver. Call `onBack()` when
UI in the scene dismisses the scene, such as a close button or a swipe.
`onBack()` requests a close of each destination that is not in the scene's
`previousEntries`.

### Overlay scenes

A scene that draws over other scenes implements `NavigationScene.Overlay`
and sets `overlaidEntries` to the destinations below the overlay.
`NavigationDisplay` runs the strategies again for the `overlaidEntries`
destinations. With an empty `overlaidEntries`, `NavigationDisplay` renders
no scene below the overlay.

Override `onRemove()` to run a dismissal animation. The overlay stays
composed until `onRemove()` returns.

## Decorate a scene

A `SceneDecoratorStrategy` wraps the scene that the scene strategy
returned. Use a `SceneDecoratorStrategy` for UI that surrounds every
screen: a navigation bar, a rail, a drawer. `NavigationDisplay` does not
apply decorators to overlay scenes. The first decorator in the list is the
innermost wrapper.

Pass decorators to `NavigationDisplay`:

<!-- snippet: scenes/decorator-display -->
```kotlin
NavigationDisplay(
    state = container,
    modifier = modifier,
    sceneStrategy = remember {
        NavigationSceneStrategy.from(
            DialogSceneStrategy(),
            DirectOverlaySceneStrategy(),
            SinglePaneSceneStrategy(),
        )
    },
    sceneDecoratorStrategies = listOf(
        remember { AdaptiveNavigationSceneDecorator(sections) },
    ),
)
```

`AdaptiveNavigationSceneDecorator` is declared in the [Scene Decoration
recipe][scene-decoration-recipe]. `AdaptiveNavigationSceneDecorator` shows
a sidebar on wide windows and a bottom bar on narrow windows.

## Transitions for a scene

`NavigationScene.metadata` defaults to the metadata of the last destination
in `entries`. A scene can override `metadata`. `NavigationDisplay` uses the
`NavigationDisplay.TransitionKey`, `PopTransitionKey` or
`PredictivePopTransitionKey` value in the scene's metadata, and uses the
`NavigationAnimations` argument when the scene's metadata has no value. See
[Animations](animations.md).

## Adaptive layouts

The Material 3 Adaptive scaffolds can be used as the content of a scene.
See the [Material List-Detail and Supporting Pane recipes][material-recipe].

## Recipes

- [Dialog][dialog-recipe]
- [Bottom Sheet][bottomsheet-recipe]
- [Two-Pane][twopane-recipe]
- [List-Detail][listdetail-recipe]
- [Material List-Detail and Supporting Pane][material-recipe]
- [Scene Decoration][scene-decoration-recipe]
- [Shell Scene][shell-scene-recipe]

## See also

- [Navigation Destinations](../core-concepts/navigation-destinations.md#destination-metadata-and-scene-strategies) — destination metadata.
- [Animations](animations.md) — transitions between scenes.

[dialog-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/dialog/DialogNavigation.kt
[bottomsheet-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/bottomsheet/BottomSheetNavigation.kt
[twopane-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/twopane/TwoPaneScene.kt
[listdetail-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/listdetail/ListDetailScene.kt
[material-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/material/MaterialAdaptiveScenes.kt
[scene-decoration-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/scenedecoration/simple/AdaptiveNavigationSceneDecorator.kt
[shell-scene-recipe]: https://github.com/isaac-udy/Enro/tree/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/scenedecoration/complex
