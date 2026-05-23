---
title: Web Platform Guide
parent: Platform-Specific Guides
nav_order: 4
---

# Web

Enro runs in the browser through Compose for Web's WasmJS target. The
install pattern mirrors the other platforms: call
`installNavigationController` once at start-up, then host a container
inside an `EnroBrowserContent`.

## Minimal install

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

The pieces:

- `installNavigationController(document)` ties the controller to the
  browser's `document`. This is currently the only supported binding —
  Enro runs in the browser via Compose for Web.
- `ComposeViewport` is the standard Compose Multiplatform entry point for
  the browser; it mounts your Compose tree at the page's root.
- `EnroBrowserContent { }` provides the Compose locals Enro needs for
  browser-specific behaviour. Treat it like Compose Multiplatform's
  outermost theme wrapper.
- `rememberInitialBackstackFromUrl { ... }` reads `window.location` once
  on first composition and tries to resolve it to a backstack via the
  controller's registered path bindings. If the URL matches a
  `@NavigationPath`, the app boots straight into that destination; if it
  doesn't, the lambda's default is used.
- `InstallWebHistoryPlugin(container)` wires your container's backstack
  into the browser history API. The URL bar reflects the current
  root-container destination, and the browser's back/forward buttons
  navigate that root backstack.

## URL routing model

Enro's web URL routing is **root-container-only** in beta:

- The URL bar always reflects the active destination of the **root
  navigation container** — the one you create with
  `rememberNavigationContainer` directly inside `EnroBrowserContent`.
- Browser back/forward navigates that root container's backstack.
- Inner-container navigation (modals, tabs, list/detail panes, anything
  hosted inside another destination) is **session-local** — it doesn't
  change the URL and doesn't create history entries.

This is the model most modern web apps use — going to a different page
on Twitter writes a URL, switching tabs within a profile doesn't.
Browser back goes between pages, not between page-internal tabs.

### What gets a URL

A `NavigationKey` annotated with `@NavigationPath` participates in URL
routing **when it is the active destination of the root container**:

```kotlin
@Serializable
@NavigationPath("/products/{productId}?source={source?}")
data class ProductDetail(
    val productId: String,
    val source: String? = null,
) : NavigationKey
```

If `ProductDetail` is at the top of the root container, the URL bar
will show `/products/abc?source=email`. If it's the top of a *nested*
container hosted inside some other destination, the URL bar continues
to show the outer (root) destination's path.

When a destination has no `@NavigationPath`, or the active destination
lives inside a nested container, the URL bar **doesn't change** — it
keeps whatever path was last set by an annotated destination (or the
URL the user originally landed on, if no annotated destination has been
active yet). `pushState` still fires, so browser back/forward continues
to work through `history.state`; the URL just doesn't pretend to
identify state that isn't bookmarkable.

### Cold loading from a URL

`rememberInitialBackstackFromUrl { default() }` reads the address bar
on first composition and resolves it through the controller's path
bindings. The resolved key becomes a single-entry backstack on the
root container. If you bookmark `/products/abc-123` and reopen it,
the app boots directly into the `ProductDetail("abc-123")` screen —
provided that destination is something you're willing to host at the
root.

If you also want pretty URLs for state that lives inside a nested
container (e.g. a list/detail pane), the synthetic-backstack approach
from the *Advanced Deep Link* recipe is the recommended pattern: read
the URL yourself, derive the parent context, and seed the backstack
manually.

## What the URL bar shows

The plugin uses two slots in `window.history`:

- **URL** (`location.pathname + location.search`) — derived from the
  root container's active destination's `@NavigationPath`. This is the
  part users see and share.
- **`history.state`** — the root container's backstack as JSON, used
  for accurate back/forward restoration mid-session.

Inner-container state is **not** serialised into either slot. If you
need it to survive page reloads, handle it via your own
`saveable`/`rememberSaveable` storage as you would on other platforms.

## What Enro provides on Web

- A real backstack for the root container that mirrors browser history.
- The full common API: `NavigationKey`, `NavigationKey.WithResult<T>`,
  `navigationHandle<T>()`, `registerForNavigationResult`,
  `NavigationDisplay`, scene strategies, plugins, decorators.
- Deep linking from a URL on cold load via
  `rememberInitialBackstackFromUrl` + `@NavigationPath` bindings on
  root destinations.
- Saved state across in-page navigation. Full-page reloads start fresh
  except for what the URL itself encodes — if you need persistence
  across reload, write to `localStorage` / `sessionStorage` yourself.

## Known limitations

- **Nested URL routing**: there's no built-in way today to encode the
  state of inner containers in the URL. A URL like `/recipe/page-2`
  that maps to `[Recipe, Page2-in-inner-container]` is something we'll
  add in a future release. For now, leaf URLs inside nested containers
  are session-local.
- **Manual address-bar edits**: if the user edits the URL by hand
  without a full-page reload, the plugin no-ops on the resulting
  `popstate`. Reloading the page applies the new URL via the cold-load
  path.

## See also

- [Installation](../getting-started/installation.md) for the multi-platform setup.
- [Recipes web main][web-main] — full working bootstrap for the recipes app.

[cmp-web]: https://github.com/JetBrains/compose-multiplatform/blob/master/web/README.md
[web-main]: https://github.com/isaac-udy/Enro/blob/main/recipes/src/wasmJsMain/kotlin/main.kt
