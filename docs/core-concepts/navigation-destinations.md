---
title: Navigation Destinations
parent: Core Concepts
nav_order: 2
---

# Navigation Destinations

A `NavigationDestination` is the *implementation* of a key — the screen
itself. Where a key is a contract ("a screen with these inputs and this
result"), a destination fulfils that contract. The two are bound together
with the `@NavigationDestination(KeyClass::class)` annotation.

Enro discovers destinations through KSP. Annotate either a `@Composable`
function or a `NavigationDestinationProvider<K>` `val`, and Enro generates
the binding code at build time.

## Two ways to declare a destination

### As a Composable function

The simplest form. Use this whenever the screen doesn't need special metadata.

```kotlin
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
```

### As a provider val

Use this when the destination needs metadata — to be rendered as a dialog, as
an overlay, or with any other behaviour controlled by a scene strategy.

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

Inside the `navigationDestination { }` content block you have a
`NavigationDestinationScope<T>` receiver — `navigation` and `key` are
properties on the scope. You also get `AnimatedVisibilityScope` and
`SharedTransitionScope` members for free, so you can use
`Modifier.animateEnterExit(...)`, shared elements, etc., directly.

## Destination metadata and scene strategies

A destination's metadata is a small typed bag attached when the destination
is *rendered*. It tells the active `NavigationSceneStrategy` how to treat
that destination — should it be rendered as a normal screen, a dialog, an
overlay above other content, or something else entirely.

The built-in metadata helpers are:

| Helper | Effect |
|---|---|
| *(none)* | The destination is rendered normally by `SinglePaneSceneStrategy` (or whichever pane-style strategy is active). |
| `dialog()` | The destination is rendered inside a Compose `Dialog`. Defaults to standard `DialogProperties`. |
| `dialog(dialogProperties = ...)` | As above with custom `DialogProperties`. |
| `directOverlay()` | The destination is rendered directly on top of the underlying scene with no window or shell. Snaps in/out by default. |
| `directOverlay(enter, exit)` | Same as `directOverlay()` but with explicit `EnterTransition` / `ExitTransition`. |
| `directOverlayWithFade(durationMillis = 128)` | Shortcut for symmetric fade in/out. |

```kotlin
@NavigationDestination(InfoSheet::class)
val infoSheetDestination = navigationDestination<InfoSheet>(
    metadata = { directOverlay() }
) {
    AlertDialog(/* ... */)
}
```

### Conditional metadata

The `metadata = { }` block runs per *instance*, so it can read the key:

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

See the [destination registration recipe][entryprovider-recipe] for a worked
example covering both styles and conditional metadata side-by-side.

## Synthetic destinations

A third declaration form, `syntheticDestination<K>`, binds a key to a block
of code instead of a UI. The key still exposes the same `open(...)` /
`complete(...)` API to callers, but when opened the synthetic runs its
block and never lands on a backstack:

```kotlin
@Serializable
object Logout : NavigationKey

@NavigationDestination(Logout::class)
val logout = syntheticDestination<Logout> {
    sessionRepository.clearSession()
    open(LoginScreen())
}
```

Synthetics are the right tool for side-effect bridges (launching an
external browser, sharing a system intent), conditional redirects (auth
gates), and "decider" patterns that pick one of several real destinations
to fulfil a single result contract.

See [Synthetic Destinations](../advanced/synthetic-destinations.md) for the
full surface — outcome methods, the result-decider pattern via
`completeFrom`, and the rules of thumb for when to reach for one.

## Scene strategies

A `NavigationSceneStrategy` decides *how* the current backstack is laid out
visually. Enro composes a chain of strategies; the first one that wants to
handle the current backstack wins. The default chain in `NavigationDisplay`
is:

```kotlin
NavigationSceneStrategy.from(
    DialogSceneStrategy(),
    DirectOverlaySceneStrategy(),
    SinglePaneSceneStrategy(),
)
```

In order: if the top entry has `dialog()` metadata, render it inside a
`Dialog`; else if it has `directOverlay()` metadata, render it as a
no-shell overlay; else fall through to the single-pane strategy and render
it like a normal screen.

You can replace the chain (or add to it) when you create a
`NavigationDisplay`:

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

The list-detail recipe shows a real adaptive scene strategy:
[`recipes/listdetail/ListDetailNavigation.kt`][listdetail-recipe].

## Custom enter/exit on overlays

For overlay destinations, the simplest way to animate them in and out is the
`directOverlay(enter, exit)` overload:

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

For animating *parts* of a destination — for example, the scrim and the card
of a dialog at different rates — see the
[Animations](../advanced/animations.md) page.

## Registering a destination without the annotation

If you'd rather not use KSP, you can register a `NavigationDestinationProvider`
manually in the module DSL:

```kotlin
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        destination(confirmDialogDestination)
        destination(itemDetailDestination)
    }
)
```

In a multi-module setup, every module that contains `@NavigationDestination`
annotations must apply the `enro-processor` KSP dependency for those
destinations to be picked up. The app module simply depends transitively on
those modules; you do not need to register the destinations again.

## Fragment and Activity destinations

Enro 3 is Compose-first, but Android Fragments and Activities are supported
through the `enro-compat` module. A Fragment or Activity is registered the
same way — `@NavigationDestination(KeyClass::class)` on the class — and the
runtime takes care of bridging it. See the
[Android platform guide](../platform/android.md).

## See also

- [Navigation Keys](navigation-keys.md) — the contract side of the equation.
- [Navigation Containers](navigation-containers.md) — where destinations live.
- [Animations](../advanced/animations.md) — animating destinations and their content.
- [Recipes][recipes] — every metadata style and several custom scene strategies.

[entryprovider-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/entryprovider/DestinationRegistration.kt
[listdetail-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/commonMain/kotlin/dev/enro/recipes/listdetail/ListDetailNavigation.kt
[recipes]: https://github.com/isaac-udy/Enro/tree/main/recipes/src/commonMain/kotlin/dev/enro/recipes
