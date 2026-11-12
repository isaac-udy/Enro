---
title: Navigation Destinations
parent: Core Concepts
nav_order: 2
---

# Navigation Destinations

A `NavigationDestination` is the implementation of the screen that a
`NavigationKey` declares. The `@NavigationDestination(KeyClass::class)`
annotation binds a destination to a key.

Annotate a `@Composable` function or a `NavigationDestinationProvider<K>`
`val`. The `enro-processor` KSP processor generates the binding code at
build time.

## Declaring a destination

### As a Composable function

Use a Composable function for a destination that declares no metadata.

<!-- snippet: navigation-destinations/composable -->
```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
```

### As a provider val

Use a provider `val` for a destination that declares metadata, for example
a destination rendered as a dialog or as an overlay.

<!-- snippet: navigation-destinations/provider -->
```kotlin
@NavigationDestination(ConfirmDialog::class)
val confirmDialogDestination: NavigationDestinationProvider<ConfirmDialog> = navigationDestination(
    metadata = {
        dialog(
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            )
        )
    },
) {
    AlertDialog(
        onDismissRequest = { navigation.close() },
        title = { Text("Confirm") },
        text = { Text("Are you sure?") },
        confirmButton = { TextButton(onClick = { navigation.close() }) { Text("Yes") } },
        dismissButton = { TextButton(onClick = { navigation.close() }) { Text("No") } },
    )
}
```

The content block of `navigationDestination { }` has a
`NavigationDestinationScope<T>` receiver. `NavigationDestinationScope<T>`
has a `navigation` property and implements `AnimatedVisibilityScope` and
`SharedTransitionScope`, so the content block can call
`Modifier.animateEnterExit(...)` and the shared element modifiers.

`navigationDestinationContent { }` declares a destination whose content has a
`NavigationDestinationContentScope<T>` receiver, which has `navigation` and
`destinationMetadata` but no animation scopes, so the content does not depend on
`NavigationDisplay`. Enro binds a `@NavigationDestination` composable function
that has no receiver as a `navigationDestinationContent` destination.

## Destination metadata and scene strategies

Destination metadata is a typed key-value map that Enro builds when Enro
creates a destination for an instance. A `NavigationSceneStrategy` reads
the destination metadata to decide how to render the destination: as a
screen, as a dialog, or as an overlay above other content.

The built-in metadata functions are:

| Function | Effect |
|---|---|
| *(none)* | `SinglePaneSceneStrategy` renders the destination as a full screen. |
| `dialog()` | `DialogSceneStrategy` renders the destination inside a Compose `Dialog` with default `DialogProperties`. |
| `dialog(dialogProperties = ...)` | The same as `dialog()`, with the given `DialogProperties`. |
| `directOverlay()` | `DirectOverlaySceneStrategy` renders the destination on top of the scene below, with no window. The destination appears and disappears without animation. |
| `directOverlay(enter, exit)` | The same as `directOverlay()`, with the given `EnterTransition` and `ExitTransition`. |
| `directOverlayWithFade(durationMillis = 128)` | The same as `directOverlay()`, with a fade in and a fade out of `durationMillis`. |

<!-- snippet: navigation-destinations/direct-overlay -->
```kotlin
@NavigationDestination(InfoSheet::class)
val infoSheetDestination = navigationDestination<InfoSheet>(
    metadata = { directOverlay() }
) {
    Surface { Text("Info") }
}
```

### Conditional metadata

Enro runs the `metadata = { }` block for each instance. The
`MetadataBuilder` receiver of the block has the `instance` and the `key`:

<!-- snippet: navigation-destinations/conditional-metadata -->
```kotlin
@Serializable
data class ItemDetail(val itemId: String, val showAsDialog: Boolean = false) : NavigationKey

@NavigationDestination(ItemDetail::class)
val itemDetailDestination = navigationDestination<ItemDetail>(
    metadata = {
        if (key.showAsDialog) dialog()
        // otherwise: rendered normally
    }
) {
    Column { Text("Item: ${navigation.key.itemId}") }
}
```

The [destination registration recipe][entryprovider-recipe] declares a
Composable function destination, a provider destination and a destination
with conditional metadata.

## Synthetic destinations

`syntheticDestination<K>` binds a key to a block of code instead of a UI.
A caller opens the key with the same `open(...)` call. Enro runs the block
and does not add the key to a backstack:

<!-- snippet: navigation-destinations/synthetic -->
```kotlin
@Serializable
object Logout : NavigationKey

@NavigationDestination(Logout::class)
val logout = syntheticDestination<Logout> {
    sessionRepository.clearSession()
    open(LoginScreen())
}
```

Use a synthetic destination to:

- run a side effect, such as launching a browser or a system share sheet;
- redirect conditionally, such as opening a login screen when no user is
  signed in;
- choose one of several destinations to produce the result for one key.

See [Synthetic Destinations](../advanced/synthetic-destinations.md) for the
functions available in the block, including `completeFrom`.

## Scene strategies

A `NavigationSceneStrategy` decides how `NavigationDisplay` lays out the
backstack. `NavigationDisplay` calls each strategy in the chain in order
and uses the first scene that a strategy returns. The default chain in
`NavigationDisplay` is:

<!-- snippet: navigation-destinations/default-chain -->
```kotlin
NavigationSceneStrategy.from(
    DialogSceneStrategy(),
    DirectOverlaySceneStrategy(),
    SinglePaneSceneStrategy(),
)
```

With the default chain, `NavigationDisplay` renders the top destination:

1. inside a `Dialog`, when the top destination has `dialog()` metadata;
2. as an overlay with no window, when the top destination has
   `directOverlay()` metadata;
3. as a full screen otherwise.

Pass `sceneStrategy` to `NavigationDisplay` to replace the chain:

<!-- snippet: navigation-destinations/custom-chain -->
```kotlin
NavigationDisplay(
    state = container,
    sceneStrategy = NavigationSceneStrategy.from(
        MyAdaptiveSceneStrategy(),
        DialogSceneStrategy(),
        DirectOverlaySceneStrategy(),
        SinglePaneSceneStrategy(),
    ),
)
```

The list-detail recipe implements an adaptive scene strategy:
[`recipes/listdetail/ListDetailNavigation.kt`][listdetail-recipe].

## Custom enter/exit on overlays

`directOverlay(enter, exit)` sets the enter and exit transitions of an
overlay destination:

<!-- snippet: navigation-destinations/overlay-transitions -->
```kotlin
@NavigationDestination(Toast::class)
val toastDestination = navigationDestination<Toast>(
    metadata = {
        directOverlay(
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
        )
    },
) {
    Surface { Text(navigation.key.message) }
}
```

To animate parts of a destination separately, for example the scrim and
the card of a dialog, see [Animations](../advanced/animations.md).

## Registering a destination without the annotation

Without KSP, register each `NavigationDestinationProvider` with
`destination(...)` in `createNavigationModule`:

<!-- snippet: navigation-destinations/manual-registration -->
```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        destination(confirmDialogDestination)
        destination(itemDetailDestination)
    }
)
```

In a multi-module project, each module that contains a
`@NavigationDestination` annotation must apply the `enro-processor` KSP
dependency. The app module depends on those modules and does not register
the annotated destinations again.

## Fragment and Activity destinations

An Android Fragment or Activity can be a destination. Annotate the class
with `@NavigationDestination(KeyClass::class)`. Fragment and Activity
destinations require no extra module. The `enro-compat` module adds the
Enro 2 API for code that has not been migrated. See the
[Android platform guide](../platform/android.md).

## See also

- [Navigation Keys](navigation-keys.md): declaring the key that a destination is bound to.
- [Navigation Containers](navigation-containers.md): the containers that display destinations.
- [Animations](../advanced/animations.md): animating destinations and parts of a destination.
- [Recipes][recipes]: examples of each metadata function and of custom scene strategies.

[entryprovider-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/entryprovider/DestinationRegistration.kt
[listdetail-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes/listdetail/ListDetailNavigation.kt
[recipes]: https://github.com/isaac-udy/Enro/tree/main/recipes/common/src/commonMain/kotlin/dev/enro/recipes
