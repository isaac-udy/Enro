---
title: Web
parent: Platform-Specific Guides
nav_order: 4
---

# Web

Enro runs in the browser on the wasmJs and Kotlin/JS targets. Call
`installNavigationController` once at start-up, then create a container
inside `EnroBrowserContent` (Compose UI) or `EnroHtmlContent`
([Compose HTML](#rendering-with-compose-html)).

## Minimal install

<!-- snippet: web/install -->
```kotlin
fun main() {
    MyComponent.installNavigationController(document)

    ComposeViewport {
        EnroBrowserContent {
            val container = rememberNavigationContainer(
                backstack = rememberInitialBackstackFromUrl {
                    backstackOf(Home.asInstance())
                },
            )
            InstallWebHistoryPlugin(container)
            NavigationDisplay(state = container)
        }
    }
}
```

- `installNavigationController(document)` installs the controller. The
  browser `document` is the only argument that the web targets accept.
- `ComposeViewport` is the Compose Multiplatform entry point for the
  browser. `ComposeViewport` renders the Compose tree in the page.
- `EnroBrowserContent { }` provides the root navigation context, the
  root navigation handle and the `ViewModelStoreOwner`. Create the root
  container inside `EnroBrowserContent`.
- `rememberInitialBackstackFromUrl { ... }` reads `window.location` once
  on first composition and resolves the URL through the controller's
  path bindings. If the URL matches a `@NavigationPath`, the returned
  backstack contains the matching key. If no path binding matches,
  `rememberInitialBackstackFromUrl` returns the result of the lambda.
- `InstallWebHistoryPlugin(container)` records the container tree under
  the root container in the browser history API. The URL bar shows the
  path of the deepest active destination, and the browser's back and
  forward buttons restore the recorded container tree.

## Rendering with Compose HTML

`enro-html` (Kotlin/JS only) renders destinations as DOM elements with
[Compose HTML][compose-html] instead of drawing the destinations on a
canvas. `enro-html` depends on `enro-runtime` and not on Compose UI, so
the bundle contains no skiko.

<!-- snippet: skip (enro-html, Kotlin/JS only) -->
```kotlin
fun main() {
    MyComponent.installNavigationController(document)

    renderComposable(rootElementId = "root") {
        EnroHtmlContent {
            val container = rememberNavigationContainer(
                backstack = rememberInitialBackstackFromUrl {
                    backstackOf(Home.asInstance())
                },
            )
            InstallWebHistoryPlugin(container)
            HtmlNavigationDisplay(container)
        }
    }
}
```

- `EnroHtmlContent { }` replaces `EnroBrowserContent`. `renderComposable`
  provides no lifecycle, ViewModelStore or SavedStateRegistry owner, so
  `EnroHtmlContent` provides each owner for the page.
- `HtmlNavigationDisplay(container)` replaces `NavigationDisplay`.
  `HtmlNavigationDisplay` changes scenes without an animation.
- A destination is a `navigationDestinationContent { }` provider or a
  `@Composable` function that emits Compose HTML elements. A Compose
  HTML destination for a key that also has a Compose UI destination is
  annotated with `@NavigationDestination.PlatformOverride` (see
  [Compose UI screens in common code](#compose-ui-screens-in-common-code)).
  `navigationDestination { }` and a function with a
  `NavigationDestinationScope` receiver are Compose UI and require
  `NavigationDisplay`.
- `htmlDialog()` metadata renders a destination in a modal `<dialog>`.
  The Escape key and a backdrop click call `requestClose()`. Style the
  dialog with the `dialog` and `dialog::backdrop` selectors.
- A `directOverlay()` destination renders over the current scene.
  `directOverlayWithFade()` fades the destination in and out.
- A closed dialog or overlay stays in the DOM until the CSS transitions
  of the dialog or overlay end, so a `dialog:not([open])` transition runs
  on close.

<!-- snippet: skip (enro-html, Kotlin/JS only) -->
```kotlin
@NavigationDestination.PlatformOverride(ConfirmDelete::class)
val confirmDeleteDestination: NavigationDestinationProvider<ConfirmDelete> =
    navigationDestinationContent(metadata = { htmlDialog() }) {
        H2 { Text("Delete ${navigation.key.item}?") }
        Button({ onClick { navigation.complete(true) } }) { Text("Delete") }
    }
```

[`tests/html-application`][html-sample] is a complete example.

### Compose UI screens in common code

A module can declare Compose UI screens in `commonMain` and Compose HTML
screens for the same keys in `jsMain`. Annotate each Compose HTML screen
with `@NavigationDestination.PlatformOverride`:

<!-- snippet: skip (two source sets; enro-html, Kotlin/JS only) -->
```kotlin
// commonMain
@Composable
@NavigationDestination(Home::class)
fun HomeScreen() { /* Compose UI */ }

// jsMain
@Composable
@NavigationDestination.PlatformOverride(Home::class)
fun HomeHtmlScreen() { /* Compose HTML */ }
```

When `enro-html` is on the Kotlin/JS classpath of the module that
declares the `@NavigationComponent`, the processor:

- registers only the override for each overridden key, so dead-code
  elimination removes the Compose UI screen from the bundle;
- fails the build with a list of the `@Composable` function destinations
  that have no override;
- warns about each property destination that has no override, because
  the processor cannot determine whether a property destination uses
  Compose UI.

The overrides can be declared in a separate module if the
`@NavigationComponent` module depends on that module.

## URL routing model

`InstallWebHistoryPlugin` records the **whole container tree** under the
root container in browser history. The root container is the container
created with `rememberNavigationContainer` directly inside
`EnroBrowserContent`.

- A push on the root container adds a history entry. A push inside a
  nested container (a screen opened in a tab, a detail pane) also adds a
  history entry, at any depth under the top destination of the root
  container.
- A change of a destination's active nested container, such as a tab
  switch, adds a history entry.
- Browser back and forward restore the recorded tree: the backstack of
  every container and the active container of each destination.
- When the app closes a screen, `InstallWebHistoryPlugin` moves browser
  history back to the matching entry and removes the forward entries, so
  browser forward does not reopen a screen that the app closed.

`InstallWebHistoryPlugin(container, nestedContainerHistory = false)`
records only the root container's backstack. The URL is then the path of
the root container's top destination.

Two changes do not add a history entry:

- A nested container that is composed for the first time (a destination
  composing its tabs, a deep link that sets the backstack of a tab) is
  added to the current history entry.
- A change that removes or swaps backstack entries, such as a root reset
  from a loading screen to a home screen, replaces the current history
  entry.

### The URL of a destination

`InstallWebHistoryPlugin` uses the `@NavigationPath` of a `NavigationKey`
as the URL of the key's destination:

<!-- snippet: web/path -->
```kotlin
@Serializable
@NavigationPath("/products/{productId}?source={source?}")
data class ProductDetail(
    val productId: String,
    val source: String? = null,
) : NavigationKey
```

The URL bar shows the path of the **deepest active destination that has
a `@NavigationPath`**. `InstallWebHistoryPlugin` searches from the root
container's top destination through each destination's active container.
If `ProductDetail` is the top destination of the root container, or the
top destination of the active tab of a destination on the root
container, the URL bar shows `/products/abc?source=email`.

When no destination in that search has a `@NavigationPath`, the URL bar
**does not change**. The URL bar shows the path of the last active
destination that had a `@NavigationPath`, or the URL that the page was
loaded with. `InstallWebHistoryPlugin` still calls `pushState`, so
browser back and forward restore the state stored in `history.state`.

### Cold loading from a URL

`rememberInitialBackstackFromUrl { default() }` reads the address bar on
first composition and resolves the URL through the controller's path
bindings. The returned backstack contains one entry: the resolved key.
Loading `/products/abc-123` starts the app with
`ProductDetail("abc-123")` as the only destination in the root
container, so `ProductDetail` must be a destination that the root
container can render.

For a URL whose destination belongs inside a nested container (a
list/detail pane, for example), use the synthetic backstack shown in the
*Advanced Deep Link* recipe: read the URL, derive the parent
destinations, and create the backstack in app code.

## URL and `history.state`

`InstallWebHistoryPlugin` writes two values to `window.history`:

- **URL** (`location.pathname + location.search`) — the `@NavigationPath`
  of the deepest active destination that has a `@NavigationPath`.
- **`history.state`** — the container tree under the root container as
  JSON: the backstack of each container and the active container of
  each destination. Browser back and forward restore the container tree
  from `history.state`.

A full-page reload restores only the state that the URL encodes. To
restore other state after a reload, save the state in app code, for
example in `localStorage` or `sessionStorage`.

## What Enro provides on Web

- Browser history for the container tree under the root container,
  including pushes in nested containers and tab switches.
- The common API: `NavigationKey`, `NavigationKey.WithResult<T>`,
  `navigationHandle<T>()`, `registerForNavigationResult`,
  `NavigationDisplay`, scene strategies, plugins, decorators.
- Deep linking on cold load with `rememberInitialBackstackFromUrl` and
  `@NavigationPath`.

## Known limitations

- **Cold loading nested state**: `rememberInitialBackstackFromUrl`
  resolves a URL to one entry in the root container. A URL whose
  destination belongs inside a nested container requires the synthetic
  backstack described in
  [Cold loading from a URL](#cold-loading-from-a-url).
  `InstallWebHistoryPlugin` records the resulting container tree as the
  first history entry.
- **Manual address-bar edits**: when the user edits the URL without a
  full-page reload, `InstallWebHistoryPlugin` ignores the resulting
  `popstate` event. A page reload applies the edited URL through
  `rememberInitialBackstackFromUrl`.

## See also

- [Installation](../getting-started/installation.md) — installation on every platform.
- [Recipes web main][web-main] — the `main` function of the recipes web app.

[web-main]: https://github.com/isaac-udy/Enro/blob/main/recipes/app/web/src/wasmJsMain/kotlin/main.kt
[compose-html]: https://github.com/JetBrains/compose-multiplatform/tree/master/html
[html-sample]: https://github.com/isaac-udy/Enro/tree/main/tests/html-application
