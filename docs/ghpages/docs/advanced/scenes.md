---
title: Scenes
parent: Advanced Topics
nav_order: 13
---

# Scenes

A scene is the layout for the visible part of a backstack. A
`NavigationDisplay` asks a `NavigationSceneStrategy` for a scene each time
the backstack changes, and renders the scene it gets.

Destinations opt in to a scene with metadata. The backstack and the keys do
not change.

## The default strategies

`NavigationDisplay` uses three strategies unless you pass your own. The
first strategy that returns a scene is used.

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
the destination: a `ModalBottomSheet`, a popup, or your own composable. The
destination below stays on screen.

Pass `directOverlay(enter, exit)` to animate the overlay in and out. The
exit transition also runs when the destination is closed from somewhere
else.

See the [Bottom Sheet recipe][bottomsheet-recipe].

## Write a scene strategy

A strategy needs a way for destinations to opt in. Define a
`NavigationDestination.MetadataKey` and a function that adds it:

<!-- snippet: scenes/metadata -->
```kotlin
internal object IsTwoPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)

/**
 * Marks a destination as eligible for two-pane rendering. The
 * [TwoPaneSceneStrategy] only renders two destinations side-by-side
 * when *both* of the top two backstack entries carry this metadata.
 */
fun NavigationDestination.MetadataBuilder<*>.twoPane() {
    add(IsTwoPaneKey, true)
}

internal fun NavigationDestination<*>.isTwoPane(): Boolean =
    metadata[IsTwoPaneKey]
```

A destination opts in with `metadata = { twoPane() }`.

The strategy receives the container's destinations and returns a
`NavigationScene`, or `null` to let the next strategy decide:

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
                    // Only one entry is popped at a time; UX-wise, going
                    // back from a two-pane scene removes the top entry only.
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
| `content` | The layout. Call `Content()` on each destination to draw it. |

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
the scene's own UI dismisses it, such as a close button or a swipe.
`NavigationDisplay` handles that call the same way as a system back press.

### Overlay scenes

A scene that draws over other scenes implements `NavigationScene.Overlay`
and sets `overlaidEntries` to the destinations below it. `NavigationDisplay`
runs the strategies again for those destinations.

Override `onRemove()` to run a dismissal animation. The overlay stays
composed until `onRemove()` returns.

## Decorate a scene

A `SceneDecoratorStrategy` wraps the scene the strategies chose. Use one for
UI that surrounds every screen: a navigation bar, a rail, a drawer.
Decorators are not applied to overlay scenes.

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

The [Scene Decoration recipe][scene-decoration-recipe] has the decorator.
It shows a sidebar on wide windows and a bottom bar on narrow windows.

## Transitions for a scene

A scene's `metadata` is the metadata of its last destination unless the
scene overrides it. `NavigationDisplay` reads
`NavigationDisplay.TransitionKey`, `PopTransitionKey` and
`PredictivePopTransitionKey` from the scene's metadata before it uses the
container's animations. See [Animations](animations.md).

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
