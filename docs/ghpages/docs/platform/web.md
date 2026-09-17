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
- `InstallWebHistoryPlugin(container)` wires the container tree under
  your root container into the browser history API. The URL bar
  reflects the deepest active destination, and the browser's
  back/forward buttons navigate whichever container the entry changed.

## URL routing model

Browser history mirrors the **whole container tree** beneath the root
container — the one you create with `rememberNavigationContainer`
directly inside `EnroBrowserContent`:

- A push on the root container is a history entry, and so is a push
  inside a nested container hosted by the destination on top of the
  root (a screen opened within a tab, a detail pane), recursively.
- Switching which nested container a destination has active — a tab
  switch — is a history entry too.
- Browser back/forward restores the recorded tree: every container's
  backstack and each destination's active container.
- Closing screens inside the app walks the browser history back the
  same way it does for the root, so back never resurrects a screen the
  app has already closed.

To keep nested navigation session-local instead — the root-only model
of earlier releases — pass `InstallWebHistoryPlugin(container,
nestedContainerHistory = false)`: only the root container's backstack
is recorded, and the URL is the root destination's path.

Two things are deliberately *not* navigation. A nested container
appearing for the first time — a destination composing its tabs, a
deep link seeding a tab several screens deep — fills in the entry it
belongs to rather than pushing one on top of it. And a change that
removes or swaps entries (a root reset such as loading → home) replaces
the current entry, since the state it overwrites is no longer reachable
in the app.

### What gets a URL

A `NavigationKey` annotated with `@NavigationPath` participates in URL
routing:

```kotlin
@Serializable
@NavigationPath("/products/{productId}?source={source?}")
data class ProductDetail(
    val productId: String,
    val source: String? = null,
) : NavigationKey
```

The URL bar shows the path of the **deepest active destination that has
one**, walking from the root container's top destination through each
destination's active container. If `ProductDetail` is on top of the
root, or on top of the active tab inside a shell on the root, the URL
bar shows `/products/abc?source=email`.

When no destination on that walk has a `@NavigationPath`, the URL bar
**doesn't change** — it keeps whatever path was last set by an annotated
destination (or the URL the user originally landed on, if no annotated
destination has been active yet). `pushState` still fires, so browser
back/forward continues to work through `history.state`; the URL just
doesn't pretend to identify state that isn't bookmarkable.

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

- **URL** (`location.pathname + location.search`) — the `@NavigationPath`
  of the deepest active destination that has one. This is the part
  users see and share.
- **`history.state`** — the container tree under the root as JSON:
  each container's backstack and each destination's active container.
  Used for accurate back/forward restoration mid-session.

A full-page reload starts from the URL alone, so only what the URL
encodes survives it. If nested state needs to survive a reload, handle
it via your own `saveable`/`rememberSaveable` storage as you would on
other platforms.

## What Enro provides on Web

- A container tree under the root that mirrors browser history —
  nested pushes and tab switches included.
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

- **Cold loading nested state**: `rememberInitialBackstackFromUrl`
  resolves a URL to a single root entry. A URL whose destination lives
  inside a nested container needs the synthetic-backstack approach
  above to seed its parents; the plugin then records that seeded tree
  as the first entry.
- **Manual address-bar edits**: if the user edits the URL by hand
  without a full-page reload, the plugin no-ops on the resulting
  `popstate`. Reloading the page applies the new URL via the cold-load
  path.

## See also

- [Installation](../getting-started/installation.md) for the multi-platform setup.
- [Recipes web main][web-main] — full working bootstrap for the recipes app.

[cmp-web]: https://github.com/JetBrains/compose-multiplatform/blob/master/web/README.md
[web-main]: https://github.com/isaac-udy/Enro/blob/main/recipes/app/web/src/wasmJsMain/kotlin/main.kt
